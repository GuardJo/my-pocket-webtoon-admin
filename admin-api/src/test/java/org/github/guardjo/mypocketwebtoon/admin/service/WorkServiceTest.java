package org.github.guardjo.mypocketwebtoon.admin.service;

import org.github.guardjo.mypocketwebtoon.admin.exception.WorkUploadException;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.WorkEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.StoredFile;
import org.github.guardjo.mypocketwebtoon.admin.repository.ThumbnailImageRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.WorkRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.impl.WorkServiceImpl;
import org.github.guardjo.mypocketwebtoon.admin.util.FileStorageUploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {
    @InjectMocks
    private WorkServiceImpl workService;

    @Mock
    private ThumbnailImageRepository thumbnailImageRepository;

    @Mock
    private WorkRepository workRepository;

    @Mock
    private FileStorageUploader fileStorageUploader;

    @DisplayName("썸네일이 없는 요청은 작품 정보만 저장한다")
    @Test
    void test_uploadWork_withoutThumbnail() {
        WorkUploadRequest uploadRequest = workUploadRequest(null);
        ArgumentCaptor<WorkEntity> workCaptor = ArgumentCaptor.forClass(WorkEntity.class);

        workService.uploadWork(uploadRequest);

        then(fileStorageUploader).shouldHaveNoInteractions();
        then(thumbnailImageRepository).shouldHaveNoInteractions();
        then(workRepository).should().save(workCaptor.capture());

        WorkEntity savedWork = workCaptor.getValue();
        assertThat(savedWork.getTitle()).isEqualTo(uploadRequest.title());
        assertThat(savedWork.getDescription()).isEqualTo(uploadRequest.description());
        assertThat(savedWork.getSerialState()).isEqualTo(uploadRequest.serialState());
        assertThat(savedWork.isVisibility()).isEqualTo(uploadRequest.visibility());
        assertThat(savedWork.getThumbnailImage()).isNull();
    }

    @DisplayName("썸네일이 있는 요청은 썸네일 저장 후 작품 정보를 저장한다")
    @Test
    void test_uploadWork_withThumbnail() {
        MockMultipartFile thumbnailFile = mockThumbnailFile();
        WorkUploadRequest uploadRequest = workUploadRequest(thumbnailFile);
        StoredFile storedFile = new StoredFile(
                "thumbnail.png",
                "stored-thumbnail.png",
                "/tmp/storage/thumbnail/stored-thumbnail.png",
                "/uploads/thumbnail/stored-thumbnail.png",
                thumbnailFile.getSize()
        );
        ArgumentCaptor<ThumbnailImageEntity> thumbnailCaptor = ArgumentCaptor.forClass(ThumbnailImageEntity.class);
        ArgumentCaptor<WorkEntity> workCaptor = ArgumentCaptor.forClass(WorkEntity.class);

        given(fileStorageUploader.upload(eq(thumbnailFile), eq("thumbnail"))).willReturn(storedFile);

        workService.uploadWork(uploadRequest);

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should(never()).delete(any(StoredFile.class));
        then(thumbnailImageRepository).should().save(thumbnailCaptor.capture());
        then(workRepository).should().save(workCaptor.capture());

        ThumbnailImageEntity savedThumbnail = thumbnailCaptor.getValue();
        WorkEntity savedWork = workCaptor.getValue();

        assertThat(savedThumbnail.getFileUrl()).isEqualTo(storedFile.publicUrl());
        assertThat(savedThumbnail.getFileSize()).isEqualTo(storedFile.size());
        assertThat(savedWork.getThumbnailImage()).isSameAs(savedThumbnail);
        assertThat(savedWork.getTitle()).isEqualTo(uploadRequest.title());
        assertThat(savedWork.getDescription()).isEqualTo(uploadRequest.description());
        assertThat(savedWork.getSerialState()).isEqualTo(uploadRequest.serialState());
        assertThat(savedWork.isVisibility()).isEqualTo(uploadRequest.visibility());
    }

    @DisplayName("썸네일 파일 저장 중 오류가 발생하면 DB 저장을 진행하지 않는다")
    @Test
    void test_uploadWork_fail_when_uploadThumbnail() {
        MockMultipartFile thumbnailFile = mockThumbnailFile();
        WorkUploadRequest uploadRequest = workUploadRequest(thumbnailFile);

        given(fileStorageUploader.upload(eq(thumbnailFile), eq("thumbnail")))
                .willThrow(new IllegalStateException("upload failed"));

        assertThatThrownBy(() -> workService.uploadWork(uploadRequest))
                .isInstanceOf(WorkUploadException.class)
                .hasMessage("작품 업로드 처리에 실패했습니다.")
                .hasCauseInstanceOf(IllegalStateException.class);

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should(never()).delete(any(StoredFile.class));
        then(thumbnailImageRepository).shouldHaveNoInteractions();
        then(workRepository).shouldHaveNoInteractions();
    }

    @DisplayName("썸네일 저장 후 작품 저장 중 오류가 발생하면 예외를 전파한다")
    @Test
    void test_uploadWork_fail_when_saveWorkAfterThumbnailSaved() {
        MockMultipartFile thumbnailFile = mockThumbnailFile();
        WorkUploadRequest uploadRequest = workUploadRequest(thumbnailFile);
        StoredFile storedFile = new StoredFile(
                "thumbnail.png",
                "stored-thumbnail.png",
                "/tmp/storage/thumbnail/stored-thumbnail.png",
                "/uploads/thumbnail/stored-thumbnail.png",
                thumbnailFile.getSize()
        );

        given(fileStorageUploader.upload(eq(thumbnailFile), eq("thumbnail"))).willReturn(storedFile);
        given(workRepository.save(any(WorkEntity.class)))
                .willThrow(new IllegalStateException("db save failed"));

        assertThatThrownBy(() -> workService.uploadWork(uploadRequest))
                .isInstanceOf(WorkUploadException.class)
                .hasMessage("작품 업로드 처리에 실패했습니다.")
                .hasCauseInstanceOf(IllegalStateException.class);

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should().delete(eq(storedFile));
        then(thumbnailImageRepository).should().save(any(ThumbnailImageEntity.class));
        then(workRepository).should().save(any(WorkEntity.class));
    }

    @DisplayName("썸네일 저장 후 데이터 무결성 오류가 발생하면 파일을 삭제하고 예외를 그대로 전파한다")
    @Test
    void test_uploadWork_fail_when_dataIntegrityViolationOccurs() {
        MockMultipartFile thumbnailFile = mockThumbnailFile();
        WorkUploadRequest uploadRequest = workUploadRequest(thumbnailFile);
        StoredFile storedFile = new StoredFile(
                "thumbnail.png",
                "stored-thumbnail.png",
                "/tmp/storage/thumbnail/stored-thumbnail.png",
                "/uploads/thumbnail/stored-thumbnail.png",
                thumbnailFile.getSize()
        );

        given(fileStorageUploader.upload(eq(thumbnailFile), eq("thumbnail"))).willReturn(storedFile);
        given(workRepository.save(any(WorkEntity.class)))
                .willThrow(new DataIntegrityViolationException("duplicated title"));

        assertThatThrownBy(() -> workService.uploadWork(uploadRequest))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("duplicated title");

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should().delete(eq(storedFile));
        then(thumbnailImageRepository).should().save(any(ThumbnailImageEntity.class));
        then(workRepository).should().save(any(WorkEntity.class));
    }

    private WorkUploadRequest workUploadRequest(MockMultipartFile thumbnailFile) {
        return new WorkUploadRequest(
                "테스트 작품",
                "작품 설명",
                "COMPLETED",
                true,
                thumbnailFile,
                new MockMultipartFile("episodeFile", "episodes.zip", "application/zip", "episode-content".getBytes())
        );
    }

    private MockMultipartFile mockThumbnailFile() {
        return new MockMultipartFile(
                "thumbnailFile",
                "thumbnail.png",
                "image/png",
                "thumbnail-content".getBytes()
        );
    }
}
