package org.github.guardjo.mypocketwebtoon.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.exception.WorkUploadException;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.WorkEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.StoredFile;
import org.github.guardjo.mypocketwebtoon.admin.repository.ThumbnailImageRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.WorkRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.WorkService;
import org.github.guardjo.mypocketwebtoon.admin.util.FileStorageUploader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        StoredFile storedThumbnailFile = null;

        try {
            ThumbnailImageEntity thumbnailImage = null;
            if (Objects.nonNull(uploadRequest.thumbnailFile()) && !uploadRequest.thumbnailFile().isEmpty()) {
                storedThumbnailFile = fileStorageUploader.upload(uploadRequest.thumbnailFile(), "thumbnail");
                thumbnailImage = saveNewThumbnailImage(storedThumbnailFile);
            }

            WorkEntity workEntity = saveNewWorkInfo(
                    uploadRequest,
                    thumbnailImage
            );

            // 작품 회차별 이미지 리소스 저장
            // 작품 회차 데이터 저장
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            rollbackThumbnailFile(storedThumbnailFile);
            throw dataIntegrityViolationException;
        } catch (RuntimeException e) {
            rollbackThumbnailFile(storedThumbnailFile);
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

        thumbnailImageRepository.save(thumbnailImageEntity);
        log.debug("Saved thumbnail_image, id = {}", thumbnailImageEntity.getId());

        return thumbnailImageEntity;
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

    private void rollbackThumbnailFile(StoredFile storedThumbnailFile) {
        if (storedThumbnailFile == null) {
            return;
        }

        try {
            fileStorageUploader.delete(storedThumbnailFile);
        } catch (RuntimeException deleteException) {
            log.warn("Failed to rollback uploaded thumbnail file, path = {}",
                    storedThumbnailFile.absolutePath(), deleteException);
        }
    }
}
