package org.github.guardjo.mypocketwebtoon.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.github.guardjo.mypocketwebtoon.admin.exception.WorkUploadException;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.WorkEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.StoredFile;
import org.github.guardjo.mypocketwebtoon.admin.repository.EpisodeImageRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.EpisodeRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.ThumbnailImageRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.WorkRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.WorkService;
import org.github.guardjo.mypocketwebtoon.admin.util.FileStorageUploader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {
    private final static String THUMBNAIL_DIRECTORY = "thumbnail";
    private final static String WORKS_DIRECTORY = "works";
    private final static Pattern IMAGE_SORT_ORDER_PATTERN = Pattern.compile(".*?(\\d+)$");
    private final static Set<String> SUPPORTED_EPISODE_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final ThumbnailImageRepository thumbnailImageRepository;
    private final EpisodeRepository episodeRepository;
    private final EpisodeImageRepository episodeImageRepository;
    private final WorkRepository workRepository;
    private final FileStorageUploader fileStorageUploader;


    @Transactional
    @Override
    public void uploadWork(WorkUploadRequest uploadRequest) {
        List<StoredFile> uploadedFiles = new ArrayList<>();

        try {
            ThumbnailImageEntity thumbnailImage = null;
            if (Objects.nonNull(uploadRequest.thumbnailFile()) && !uploadRequest.thumbnailFile().isEmpty()) {
                StoredFile storedThumbnailFile = fileStorageUploader.upload(uploadRequest.thumbnailFile(), THUMBNAIL_DIRECTORY);
                uploadedFiles.add(storedThumbnailFile);
                thumbnailImage = saveNewThumbnailImage(storedThumbnailFile);
            }

            WorkEntity workEntity = saveNewWorkInfo(
                    uploadRequest,
                    thumbnailImage
            );

            uploadEpisodes(uploadRequest.episodeFile(), workEntity, uploadedFiles);

            log.info("Uploaded, new work, id = {}, title = {}", workEntity.getId(), workEntity.getTitle());
        } catch (DataIntegrityViolationException | WorkUploadException dataIntegrityViolationException) {
            rollbackUploadedFiles(uploadedFiles);
            throw dataIntegrityViolationException;
        } catch (RuntimeException e) {
            rollbackUploadedFiles(uploadedFiles);
            throw new WorkUploadException("작품 업로드 처리에 실패했습니다.", e);
        }
    }

    /*
    작품 썸네일 데이터 및 파일 저장
     */
    private ThumbnailImageEntity saveNewThumbnailImage(StoredFile storedFile) {
        log.debug("Uploaded thumbnailFile, originName = {}, uploadName = {}", storedFile.originalFilename(), storedFile.storedFilename());

        ThumbnailImageEntity thumbnailImageEntity = ThumbnailImageEntity.builder()
                .fileUrl(storedFile.publicUrl())
                .fileSize(storedFile.size())
                .build();

        ThumbnailImageEntity savedThumbnailImage = thumbnailImageRepository.save(thumbnailImageEntity);
        log.debug("Saved thumbnail_image, id = {}", savedThumbnailImage.getId());

        return savedThumbnailImage;
    }

    /*
    작품 원장 데이터 저장
     */
    private WorkEntity saveNewWorkInfo(WorkUploadRequest uploadRequest, ThumbnailImageEntity thumbnailImage) {
        WorkEntity workEntity = WorkEntity.builder()
                .title(uploadRequest.title())
                .description(uploadRequest.description())
                .serialState(uploadRequest.serialState())
                .visibility(uploadRequest.visibility())
                .thumbnailImage(thumbnailImage)
                .build();

        WorkEntity savedWorkEntity = workRepository.save(workEntity);

        log.debug("Uploaded work, workId = {}", savedWorkEntity.getId());

        return savedWorkEntity;
    }

    private void rollbackUploadedFiles(List<StoredFile> uploadedFiles) {
        log.warn("Rollback upload files, totalCount = {}", uploadedFiles.size());

        for (int i = uploadedFiles.size() - 1; i >= 0; i--) {
            StoredFile uploadedFile = uploadedFiles.get(i);

            try {
                fileStorageUploader.delete(uploadedFile);
            } catch (RuntimeException deleteException) {
                log.warn("Failed to rollback uploaded file, path = {}", uploadedFile.absolutePath(), deleteException);
            }
        }
    }

    /*
    tar 파일 내 에피소드 리소스 추출 및 저장
     */
    private void uploadEpisodes(MultipartFile episodeFile, WorkEntity workEntity, List<StoredFile> uploadedFiles) {
        validateEpisodeFile(episodeFile);

        Map<Integer, EpisodeDraft> episodeDrafts = new TreeMap<>();

        try (InputStream inputStream = episodeFile.getInputStream();
             TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(inputStream)) {

            TarArchiveEntry tarArchiveEntry = null;

            while ((tarArchiveEntry = tarArchiveInputStream.getNextEntry()) != null) {
                String normalizedEntryName = normalizeTarEntryName(tarArchiveEntry.getName());

                if (shouldSkipEntry(tarArchiveEntry, normalizedEntryName)) {
                    continue;
                }

                EpisodeArchiveEntry archiveEntry = parseEpisodeArchiveEntry(normalizedEntryName);
                EpisodeDraft episodeDraft = episodeDrafts.computeIfAbsent(
                        archiveEntry.episodeNo(),
                        EpisodeDraft::new
                );
                episodeDraft.validateSortOrderAvailable(archiveEntry.sortOrder(), normalizedEntryName);

                byte[] entryContent = tarArchiveInputStream.readAllBytes();
                StoredFile storedEpisodeImage = fileStorageUploader.upload(
                        new ByteArrayInputStream(entryContent),
                        archiveEntry.fileName(),
                        buildEpisodeDirectory(workEntity, archiveEntry.episodeNo())
                );

                uploadedFiles.add(storedEpisodeImage);
                episodeDraft.addImage(archiveEntry.sortOrder(), storedEpisodeImage);
            }
        } catch (IOException e) {
            throw new WorkUploadException("회차 tar 파일을 읽지 못했습니다.", e);
        }

        if (episodeDrafts.isEmpty()) {
            throw new WorkUploadException("회차 tar 파일에서 저장할 이미지 리소스를 찾지 못했습니다.");
        }

        persistEpisodes(workEntity, episodeDrafts);
    }

    /*
    에피소드 리소스 파일 검증
     */
    private void validateEpisodeFile(MultipartFile episodeFile) {
        if (episodeFile == null || episodeFile.isEmpty()) {
            throw new WorkUploadException("회차 tar 파일이 비어 있습니다.");
        }
    }

    /*
    작품 데이터 및 회차별 이미지 리소스 영속화
     */
    private void persistEpisodes(WorkEntity workEntity, Map<Integer, EpisodeDraft> episodeDrafts) {
        List<EpisodeDraft> sortedEpisodeDrafts = new ArrayList<>(episodeDrafts.values());
        List<ThumbnailImageEntity> episodeThumbnails = new ArrayList<>(sortedEpisodeDrafts.size());
        List<EpisodeEntity> episodes = new ArrayList<>(sortedEpisodeDrafts.size());

        for (EpisodeDraft episodeDraft : sortedEpisodeDrafts) {
            UploadedEpisodeImage thumbnailSource = episodeDraft.firstImage();
            ThumbnailImageEntity thumbnailImageEntity = ThumbnailImageEntity.builder()
                    .fileUrl(thumbnailSource.storedFile().publicUrl())
                    .fileSize(thumbnailSource.storedFile().size())
                    .build();

            episodeThumbnails.add(thumbnailImageEntity);
            episodes.add(EpisodeEntity.builder()
                    .work(workEntity)
                    .episodeNo(episodeDraft.episodeNo())
                    .thumbnailImage(thumbnailImageEntity)
                    .likeCount(0)
                    .viewCount(0)
                    .build());
        }

        thumbnailImageRepository.saveAll(episodeThumbnails);
        episodeRepository.saveAll(episodes);

        List<EpisodeImageEntity> episodeImages = new ArrayList<>();
        for (int i = 0; i < sortedEpisodeDrafts.size(); i++) {
            EpisodeDraft episodeDraft = sortedEpisodeDrafts.get(i);
            EpisodeEntity episodeEntity = episodes.get(i);

            for (UploadedEpisodeImage uploadedEpisodeImage : episodeDraft.images()) {
                episodeImages.add(EpisodeImageEntity.builder()
                        .episode(episodeEntity)
                        .sort_order(uploadedEpisodeImage.sortOrder())
                        .fileUrl(uploadedEpisodeImage.storedFile().publicUrl())
                        .fileSize(uploadedEpisodeImage.storedFile().size())
                        .build());
            }
        }

        episodeImageRepository.saveAll(episodeImages);

        log.debug("Saved episodes for workId = {}, episodeCount = {}, imageCount = {}",
                workEntity.getId(), episodes.size(), episodeImages.size());
    }

    /*
    작품별 스토리지 저장 경로 디렉터리 구성
     */
    private String buildEpisodeDirectory(WorkEntity workEntity, int episodeNo) {
        if (workEntity.getId() == null) {
            throw new WorkUploadException("회차 이미지 저장 경로를 구성할 작품 ID가 없습니다.");
        }

        return WORKS_DIRECTORY + "/" + workEntity.getId() + "/" + episodeNo;
    }

    private EpisodeArchiveEntry parseEpisodeArchiveEntry(String entryName) {
        String[] segments = Arrays.stream(entryName.split("/"))
                .filter(StringUtils::hasText)
                .toArray(String[]::new);

        if (segments.length < 2) {
            throw new WorkUploadException("회차 tar 구조가 올바르지 않습니다. entry = " + entryName);
        }

        String fileName = segments[segments.length - 1];
        String episodeDirectory = segments[segments.length - 2];

        validateEpisodeImageFilename(fileName, entryName);

        return new EpisodeArchiveEntry(
                parseEpisodeNo(episodeDirectory, entryName),
                parseSortOrder(fileName, entryName),
                fileName
        );
    }

    /*
    에피소드 넘버링 추출
     */
    private int parseEpisodeNo(String episodeDirectory, String entryName) {
        try {
            return Integer.parseInt(episodeDirectory);
        } catch (NumberFormatException e) {
            throw new WorkUploadException("회차 디렉터리명은 정수여야 합니다. entry = " + entryName, e);
        }
    }

    /*
    에피소드 내 이미지 정렬 순서 추출
     */
    private int parseSortOrder(String fileName, String entryName) {
        String fileNameWithoutExtension = removeFileExtension(fileName);
        Matcher matcher = IMAGE_SORT_ORDER_PATTERN.matcher(Objects.requireNonNullElse(fileNameWithoutExtension, ""));

        if (!matcher.matches()) {
            throw new WorkUploadException("회차 이미지 파일명에서 정렬 순서를 추출할 수 없습니다. entry = " + entryName);
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new WorkUploadException("회차 이미지 정렬 순서가 올바르지 않습니다. entry = " + entryName, e);
        }
    }

    /*
    이미지 리소스 파일명 검증
     */
    private void validateEpisodeImageFilename(String fileName, String entryName) {
        if (!StringUtils.hasText(fileName)) {
            throw new WorkUploadException("회차 이미지 파일명이 비어 있습니다. entry = " + entryName);
        }

        String extension = StringUtils.getFilenameExtension(fileName);
        if (!StringUtils.hasText(extension)
                || !SUPPORTED_EPISODE_IMAGE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new WorkUploadException("지원하지 않는 회차 이미지 형식입니다. entry = " + entryName);
        }
    }

    /*
    파일 리소스 경로 정리
     */
    private String normalizeTarEntryName(String entryName) {
        String normalizedEntryName = entryName.replace("\\", "/").trim();
        while (normalizedEntryName.startsWith("./")) {
            normalizedEntryName = normalizedEntryName.substring(2);
        }
        return normalizedEntryName;
    }

    /*
    파일 확장자명 제거
     */
    private String removeFileExtension(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex < 0) {
            return fileName;
        }
        return fileName.substring(0, extensionIndex);
    }

    /*
    하위 파일 작업 여부 체크
     */
    private boolean shouldSkipEntry(TarArchiveEntry tarArchiveEntry, String entryName) {
        if (tarArchiveEntry.isDirectory() || !StringUtils.hasText(entryName)) {
            return true;
        }

        String[] segments = Arrays.stream(entryName.split("/"))
                .filter(StringUtils::hasText)
                .toArray(String[]::new);
        if (segments.length == 0) {
            return true;
        }

        String fileName = segments[segments.length - 1];
        return Arrays.stream(segments).anyMatch("__MACOSX"::equals)
                || ".DS_Store".equalsIgnoreCase(fileName)
                || fileName.startsWith("._")
                || "@PaxHeader".equals(fileName)
                || entryName.contains("PaxHeaders");
    }

    private record EpisodeArchiveEntry(
            int episodeNo,
            int sortOrder,
            String fileName
    ) {
    }

    private record UploadedEpisodeImage(
            int sortOrder,
            StoredFile storedFile
    ) {
    }

    /*
    작품 회차 별 이미지 리소스 처리용 VO
     */
    private static final class EpisodeDraft {
        private final int episodeNo;
        private final Map<Integer, UploadedEpisodeImage> imagesBySortOrder = new TreeMap<>();

        private EpisodeDraft(int episodeNo) {
            this.episodeNo = episodeNo;
        }

        private int episodeNo() {
            return episodeNo;
        }

        /*
        회차별 내부 정렬 순서 검증
         */
        private void validateSortOrderAvailable(int sortOrder, String entryName) {
            if (imagesBySortOrder.containsKey(sortOrder)) {
                throw new WorkUploadException("동일 회차 내 중복된 이미지 순서가 존재합니다. entry = " + entryName);
            }
        }

        private void addImage(int sortOrder, StoredFile storedFile) {
            imagesBySortOrder.put(sortOrder, new UploadedEpisodeImage(sortOrder, storedFile));
        }

        private UploadedEpisodeImage firstImage() {
            return imagesBySortOrder.values().stream()
                    .findFirst()
                    .orElseThrow(() -> new WorkUploadException("에피소드 이미지 정보가 비어 있습니다."));
        }

        private List<UploadedEpisodeImage> images() {
            return new ArrayList<>(imagesBySortOrder.values());
        }
    }
}
