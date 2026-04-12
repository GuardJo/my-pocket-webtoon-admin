package org.github.guardjo.mypocketwebtoon.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.WorkEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.StoredFile;
import org.github.guardjo.mypocketwebtoon.admin.repository.ThumbnailImageRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.WorkRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.WorkService;
import org.github.guardjo.mypocketwebtoon.admin.util.FileStorageUploader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {
    private final ThumbnailImageRepository thumbnailImageRepository;
    private final WorkRepository workRepository;
    private final FileStorageUploader fileStorageUploader;


    @Transactional
    @Override
    public void uploadWork(WorkUploadRequest uploadRequest) {
        StoredThumbnail storedThumbnail = null;

        try {
            WorkEntity workEntity = saveNewWorkInfo(
                    uploadRequest,
                    Objects.isNull(uploadRequest.thumbnailFile()) ? null : (storedThumbnail = saveNewThumbnailImage(uploadRequest.thumbnailFile())).thumbnailImage()
            );

            // 작품 회차별 이미지 리소스 저장
            // 작품 회차 데이터 저장
        } catch (RuntimeException e) {
            rollbackThumbnailFile(storedThumbnail);
            throw e;
        }
    }

    /*
    작품 썸네일 데이터 및 파일 저장
     */
    private StoredThumbnail saveNewThumbnailImage(MultipartFile thumbnailFile) {
        StoredFile storedFile = fileStorageUploader.upload(thumbnailFile, "thumbnail");
        log.debug("Uploaded thumbnailFile, originName = {}, uploadName = {}", storedFile.originalFilename(), storedFile.storedFilename());

        ThumbnailImageEntity thumbnailImageEntity = ThumbnailImageEntity.builder()
                .fileUrl(storedFile.publicUrl())
                .fileSize(storedFile.size())
                .build();

        thumbnailImageRepository.save(thumbnailImageEntity);
        log.debug("Saved thumbnail_image, id = {}", thumbnailImageEntity.getId());

        return new StoredThumbnail(storedFile, thumbnailImageEntity);
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

        workRepository.save(workEntity);

        log.debug("Uploaded work, workId = {}", workEntity.getId());

        return workEntity;
    }

    private void rollbackThumbnailFile(StoredThumbnail storedThumbnail) {
        if (storedThumbnail == null) {
            return;
        }

        try {
            fileStorageUploader.delete(storedThumbnail.storedFile());
        } catch (RuntimeException deleteException) {
            log.warn("Failed to rollback uploaded thumbnail file, path = {}",
                    storedThumbnail.storedFile().absolutePath(), deleteException);
        }
    }

    private record StoredThumbnail(
            StoredFile storedFile,
            ThumbnailImageEntity thumbnailImage
    ) {
    }
}
