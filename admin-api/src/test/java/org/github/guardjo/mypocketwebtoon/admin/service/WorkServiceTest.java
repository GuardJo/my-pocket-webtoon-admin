package org.github.guardjo.mypocketwebtoon.admin.service;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class WorkServiceTest {
    @InjectMocks
    private WorkServiceImpl workService;

    @Mock
    private ThumbnailImageRepository thumbnailImageRepository;

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private EpisodeImageRepository episodeImageRepository;

    @Mock
    private WorkRepository workRepository;

    @Mock
    private FileStorageUploader fileStorageUploader;

    @DisplayName("썸네일이 없는 요청은 작품과 에피소드 정보를 함께 저장한다")
    @Test
    void test_uploadWork_withoutThumbnail() {
        WorkUploadRequest uploadRequest = workUploadRequest(null);
        stubSavedWork(1L);
        stubEpisodeUploads();

        ArgumentCaptor<WorkEntity> workCaptor = ArgumentCaptor.forClass(WorkEntity.class);
        ArgumentCaptor<Iterable> episodeThumbnailCaptor = ArgumentCaptor.forClass(Iterable.class);
        ArgumentCaptor<Iterable> episodeCaptor = ArgumentCaptor.forClass(Iterable.class);
        ArgumentCaptor<Iterable> episodeImageCaptor = ArgumentCaptor.forClass(Iterable.class);

        workService.uploadWork(uploadRequest);

        then(fileStorageUploader).should(never()).upload(any(MockMultipartFile.class), eq("thumbnail"));
        then(fileStorageUploader).should(times(3)).upload(any(InputStream.class), anyString(), anyString());
        then(fileStorageUploader).should(never()).delete(any(StoredFile.class));
        then(thumbnailImageRepository).should(never()).save(any(ThumbnailImageEntity.class));
        then(thumbnailImageRepository).should().saveAll(episodeThumbnailCaptor.capture());
        then(workRepository).should().save(workCaptor.capture());
        then(episodeRepository).should().saveAll(episodeCaptor.capture());
        then(episodeImageRepository).should().saveAll(episodeImageCaptor.capture());

        WorkEntity savedWork = workCaptor.getValue();
        List<ThumbnailImageEntity> savedEpisodeThumbnails = toList((Iterable<ThumbnailImageEntity>) episodeThumbnailCaptor.getValue());
        List<EpisodeEntity> savedEpisodes = toList((Iterable<EpisodeEntity>) episodeCaptor.getValue());
        List<EpisodeImageEntity> savedEpisodeImages = toList((Iterable<EpisodeImageEntity>) episodeImageCaptor.getValue());

        assertThat(savedWork.getTitle()).isEqualTo(uploadRequest.title());
        assertThat(savedWork.getThumbnailImage()).isNull();
        assertThat(savedEpisodeThumbnails)
                .extracting(ThumbnailImageEntity::getFileUrl)
                .containsExactly(
                        "/uploads/works/1/1/view-padding-02-img-001.jpg",
                        "/uploads/works/1/2/view-padding-02-img-001.jpg"
                );
        assertThat(savedEpisodes)
                .extracting(EpisodeEntity::getEpisodeNo, EpisodeEntity::getThumbnailImage)
                .containsExactly(
                        tuple(1, savedEpisodeThumbnails.get(0)),
                        tuple(2, savedEpisodeThumbnails.get(1))
                );
        assertThat(savedEpisodeImages)
                .extracting("sort_order", "fileUrl")
                .containsExactly(
                        tuple(1, "/uploads/works/1/1/view-padding-02-img-001.jpg"),
                        tuple(2, "/uploads/works/1/1/view-padding-02-img-002.jpg"),
                        tuple(1, "/uploads/works/1/2/view-padding-02-img-001.jpg")
                );
    }

    @DisplayName("썸네일이 있는 요청은 작품 썸네일과 에피소드 정보를 모두 저장한다")
    @Test
    void test_uploadWork_withThumbnail() {
        MockMultipartFile thumbnailFile = mockThumbnailFile();
        WorkUploadRequest uploadRequest = workUploadRequest(thumbnailFile);
        StoredFile storedThumbnailFile = new StoredFile(
                "thumbnail.png",
                "stored-thumbnail.png",
                "/tmp/storage/thumbnail/stored-thumbnail.png",
                "/uploads/thumbnail/stored-thumbnail.png",
                thumbnailFile.getSize()
        );
        ArgumentCaptor<ThumbnailImageEntity> thumbnailCaptor = ArgumentCaptor.forClass(ThumbnailImageEntity.class);
        ArgumentCaptor<WorkEntity> workCaptor = ArgumentCaptor.forClass(WorkEntity.class);

        given(fileStorageUploader.upload(eq(thumbnailFile), eq("thumbnail"))).willReturn(storedThumbnailFile);
        given(thumbnailImageRepository.save(any(ThumbnailImageEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        stubSavedWork(1L);
        stubEpisodeUploads();

        workService.uploadWork(uploadRequest);

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should(times(3)).upload(any(InputStream.class), anyString(), anyString());
        then(fileStorageUploader).should(never()).delete(any(StoredFile.class));
        then(thumbnailImageRepository).should().save(thumbnailCaptor.capture());
        then(thumbnailImageRepository).should().saveAll(anyIterable());
        then(workRepository).should().save(workCaptor.capture());
        then(episodeRepository).should().saveAll(anyIterable());
        then(episodeImageRepository).should().saveAll(anyIterable());

        ThumbnailImageEntity savedThumbnail = thumbnailCaptor.getValue();
        WorkEntity savedWork = workCaptor.getValue();

        assertThat(savedThumbnail.getFileUrl()).isEqualTo(storedThumbnailFile.publicUrl());
        assertThat(savedThumbnail.getFileSize()).isEqualTo(storedThumbnailFile.size());
        assertThat(savedWork.getThumbnailImage()).isSameAs(savedThumbnail);
        assertThat(savedWork.getTitle()).isEqualTo(uploadRequest.title());
        assertThat(savedWork.getDescription()).isEqualTo(uploadRequest.description());
        assertThat(savedWork.getSerialState()).isEqualTo(uploadRequest.serialState());
        assertThat(savedWork.isVisibility()).isEqualTo(uploadRequest.visibility());
    }

    @DisplayName("회차 이미지 업로드 시 tar 엔트리별 독립된 스트림을 전달한다")
    @Test
    void test_uploadWork_uploadsIndependentEpisodeEntryStreams() {
        WorkUploadRequest uploadRequest = workUploadRequest(null);
        stubSavedWork(1L);
        given(fileStorageUploader.upload(any(InputStream.class), anyString(), anyString()))
                .willAnswer(invocation -> {
                    InputStream inputStream = invocation.getArgument(0, InputStream.class);
                    String originalFilename = invocation.getArgument(1, String.class);
                    String directory = invocation.getArgument(2, String.class);
                    byte[] content = inputStream.readAllBytes();

                    return new StoredFile(
                            originalFilename,
                            originalFilename,
                            "/tmp/storage/" + directory + "/" + originalFilename,
                            new String(content, StandardCharsets.UTF_8),
                            content.length
                    );
                });

        workService.uploadWork(uploadRequest);

        ArgumentCaptor<Iterable> episodeImageCaptor = ArgumentCaptor.forClass(Iterable.class);
        then(episodeImageRepository).should().saveAll(episodeImageCaptor.capture());

        List<EpisodeImageEntity> savedEpisodeImages = toList((Iterable<EpisodeImageEntity>) episodeImageCaptor.getValue());
        assertThat(savedEpisodeImages)
                .extracting("sort_order", "fileUrl", "fileSize")
                .containsExactly(
                        tuple(1, "episode-1-image-1", 17L),
                        tuple(2, "episode-1-image-2", 17L),
                        tuple(1, "episode-2-image-1", 17L)
                );
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
        then(fileStorageUploader).should(never()).upload(any(InputStream.class), anyString(), anyString());
        then(fileStorageUploader).should(never()).delete(any(StoredFile.class));
        then(thumbnailImageRepository).shouldHaveNoInteractions();
        then(workRepository).shouldHaveNoInteractions();
        then(episodeRepository).shouldHaveNoInteractions();
        then(episodeImageRepository).shouldHaveNoInteractions();
    }

    @DisplayName("썸네일 저장 후 작품 저장 중 오류가 발생하면 썸네일 파일만 롤백한다")
    @Test
    void test_uploadWork_fail_when_saveWorkAfterThumbnailSaved() {
        MockMultipartFile thumbnailFile = mockThumbnailFile();
        WorkUploadRequest uploadRequest = workUploadRequest(thumbnailFile);
        StoredFile storedThumbnailFile = new StoredFile(
                "thumbnail.png",
                "stored-thumbnail.png",
                "/tmp/storage/thumbnail/stored-thumbnail.png",
                "/uploads/thumbnail/stored-thumbnail.png",
                thumbnailFile.getSize()
        );

        given(fileStorageUploader.upload(eq(thumbnailFile), eq("thumbnail"))).willReturn(storedThumbnailFile);
        given(thumbnailImageRepository.save(any(ThumbnailImageEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(workRepository.save(any(WorkEntity.class)))
                .willThrow(new IllegalStateException("db save failed"));

        assertThatThrownBy(() -> workService.uploadWork(uploadRequest))
                .isInstanceOf(WorkUploadException.class)
                .hasMessage("작품 업로드 처리에 실패했습니다.")
                .hasCauseInstanceOf(IllegalStateException.class);

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should(never()).upload(any(InputStream.class), anyString(), anyString());
        then(fileStorageUploader).should().delete(eq(storedThumbnailFile));
        then(thumbnailImageRepository).should().save(any(ThumbnailImageEntity.class));
        then(workRepository).should().save(any(WorkEntity.class));
        then(episodeRepository).shouldHaveNoInteractions();
        then(episodeImageRepository).shouldHaveNoInteractions();
    }

    @DisplayName("썸네일 저장 후 데이터 무결성 오류가 발생하면 파일을 삭제하고 예외를 그대로 전파한다")
    @Test
    void test_uploadWork_fail_when_dataIntegrityViolationOccurs() {
        MockMultipartFile thumbnailFile = mockThumbnailFile();
        WorkUploadRequest uploadRequest = workUploadRequest(thumbnailFile);
        StoredFile storedThumbnailFile = new StoredFile(
                "thumbnail.png",
                "stored-thumbnail.png",
                "/tmp/storage/thumbnail/stored-thumbnail.png",
                "/uploads/thumbnail/stored-thumbnail.png",
                thumbnailFile.getSize()
        );

        given(fileStorageUploader.upload(eq(thumbnailFile), eq("thumbnail"))).willReturn(storedThumbnailFile);
        given(thumbnailImageRepository.save(any(ThumbnailImageEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(workRepository.save(any(WorkEntity.class)))
                .willThrow(new DataIntegrityViolationException("duplicated title"));

        assertThatThrownBy(() -> workService.uploadWork(uploadRequest))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("duplicated title");

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should(never()).upload(any(InputStream.class), anyString(), anyString());
        then(fileStorageUploader).should().delete(eq(storedThumbnailFile));
        then(thumbnailImageRepository).should().save(any(ThumbnailImageEntity.class));
        then(workRepository).should().save(any(WorkEntity.class));
        then(episodeRepository).shouldHaveNoInteractions();
        then(episodeImageRepository).shouldHaveNoInteractions();
    }

    @DisplayName("에피소드 메타데이터 저장 중 오류가 발생하면 업로드한 모든 파일을 롤백한다")
    @Test
    void test_uploadWork_fail_when_saveEpisodesAfterUploadingImages() {
        MockMultipartFile thumbnailFile = mockThumbnailFile();
        WorkUploadRequest uploadRequest = workUploadRequest(thumbnailFile);
        StoredFile storedThumbnailFile = new StoredFile(
                "thumbnail.png",
                "stored-thumbnail.png",
                "/tmp/storage/thumbnail/stored-thumbnail.png",
                "/uploads/thumbnail/stored-thumbnail.png",
                thumbnailFile.getSize()
        );
        ArgumentCaptor<StoredFile> deletedFileCaptor = ArgumentCaptor.forClass(StoredFile.class);

        given(fileStorageUploader.upload(eq(thumbnailFile), eq("thumbnail"))).willReturn(storedThumbnailFile);
        given(thumbnailImageRepository.save(any(ThumbnailImageEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        stubSavedWork(1L);
        stubEpisodeUploads();
        given(episodeRepository.saveAll(anyIterable()))
                .willThrow(new IllegalStateException("episode save failed"));

        assertThatThrownBy(() -> workService.uploadWork(uploadRequest))
                .isInstanceOf(WorkUploadException.class)
                .hasMessage("작품 업로드 처리에 실패했습니다.")
                .hasCauseInstanceOf(IllegalStateException.class);

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should(times(3)).upload(any(InputStream.class), anyString(), anyString());
        then(fileStorageUploader).should(times(4)).delete(deletedFileCaptor.capture());

        assertThat(deletedFileCaptor.getAllValues())
                .extracting(StoredFile::publicUrl)
                .containsExactly(
                        "/uploads/works/1/2/view-padding-02-img-001.jpg",
                        "/uploads/works/1/1/view-padding-02-img-002.jpg",
                        "/uploads/works/1/1/view-padding-02-img-001.jpg",
                        "/uploads/thumbnail/stored-thumbnail.png"
                );
    }

    private void stubSavedWork(long workId) {
        given(workRepository.save(any(WorkEntity.class)))
                .willAnswer(invocation -> {
                    WorkEntity workEntity = invocation.getArgument(0);
                    return WorkEntity.builder()
                            .id(workId)
                            .title(workEntity.getTitle())
                            .description(workEntity.getDescription())
                            .serialState(workEntity.getSerialState())
                            .thumbnailImage(workEntity.getThumbnailImage())
                            .visibility(workEntity.isVisibility())
                            .build();
                });
    }

    private void stubEpisodeUploads() {
        given(fileStorageUploader.upload(any(InputStream.class), anyString(), anyString()))
                .willAnswer(invocation -> {
                    String originalFilename = invocation.getArgument(1, String.class);
                    String directory = invocation.getArgument(2, String.class);

                    return new StoredFile(
                            originalFilename,
                            originalFilename,
                            "/tmp/storage/" + directory + "/" + originalFilename,
                            "/uploads/" + directory + "/" + originalFilename,
                            1024L
                    );
                });
    }

    private WorkUploadRequest workUploadRequest(MockMultipartFile thumbnailFile) {
        return new WorkUploadRequest(
                "테스트 작품",
                "작품 설명",
                "COMPLETED",
                true,
                thumbnailFile,
                mockEpisodeTarFile()
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

    private MockMultipartFile mockEpisodeTarFile() {
        return new MockMultipartFile(
                "episodeFile",
                "episodes.tar",
                "application/x-tar",
                createEpisodeTar()
        );
    }

    private byte[] createEpisodeTar() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(outputStream)) {

            tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            addTarEntry(tarOutputStream, "sample-work/1/view-padding-02-img-001.jpg", "episode-1-image-1".getBytes());
            addTarEntry(tarOutputStream, "sample-work/1/view-padding-02-img-002.jpg", "episode-1-image-2".getBytes());
            addTarEntry(tarOutputStream, "sample-work/2/view-padding-02-img-001.jpg", "episode-2-image-1".getBytes());
            tarOutputStream.finish();

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void addTarEntry(TarArchiveOutputStream tarOutputStream, String entryName, byte[] content) throws IOException {
        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(entryName);
        tarArchiveEntry.setSize(content.length);

        tarOutputStream.putArchiveEntry(tarArchiveEntry);
        tarOutputStream.write(content);
        tarOutputStream.closeArchiveEntry();
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }
}
