package org.github.guardjo.mypocketwebtoon.admin.service;

import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.StorageProperties;
import org.github.guardjo.mypocketwebtoon.admin.exception.WorkFileStorageException;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.WorkEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUpdateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.EpisodeInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.StoredFile;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkSummary;
import org.github.guardjo.mypocketwebtoon.admin.repository.EpisodeImageRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.EpisodeRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.ThumbnailImageRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.WorkRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.impl.WorkServiceImpl;
import org.github.guardjo.mypocketwebtoon.admin.util.FileStorageUploader;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class WorkServiceTest {
    private WorkServiceImpl workService;
    private ExecutorService episodeUploadExecutor;

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

    @BeforeEach
    void setUp() {
        this.episodeUploadExecutor = Executors.newFixedThreadPool(2);
        StorageProperties storageProperties = new StorageProperties(12);
        this.workService = new WorkServiceImpl(
                thumbnailImageRepository,
                episodeRepository,
                episodeImageRepository,
                workRepository,
                fileStorageUploader,
                episodeUploadExecutor,
                storageProperties
        );
    }

    @AfterEach
    void tearDown() {
        episodeUploadExecutor.shutdownNow();
    }

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
        then(fileStorageUploader).should(times(3)).upload(any(byte[].class), anyString(), anyString());
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
                .extracting("sortOrder", "fileUrl")
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
        then(fileStorageUploader).should(times(3)).upload(any(byte[].class), anyString(), anyString());
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

    @DisplayName("회차 이미지 업로드 시 tar 엔트리별 독립된 파일 내용을 전달한다")
    @Test
    void test_uploadWork_uploadsIndependentEpisodeEntryContents() {
        WorkUploadRequest uploadRequest = workUploadRequest(null);
        stubSavedWork(1L);
        given(fileStorageUploader.upload(any(byte[].class), anyString(), anyString()))
                .willAnswer(invocation -> {
                    byte[] content = invocation.getArgument(0, byte[].class);
                    String originalFilename = invocation.getArgument(1, String.class);
                    String directory = invocation.getArgument(2, String.class);

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
                .extracting("sortOrder", "fileUrl", "fileSize")
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
                .isInstanceOf(WorkFileStorageException.class)
                .hasMessage("작품 업로드 처리에 실패했습니다.")
                .hasCauseInstanceOf(IllegalStateException.class);

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should(never()).upload(any(byte[].class), anyString(), anyString());
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
                .isInstanceOf(WorkFileStorageException.class)
                .hasMessage("작품 업로드 처리에 실패했습니다.")
                .hasCauseInstanceOf(IllegalStateException.class);

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should(never()).upload(any(byte[].class), anyString(), anyString());
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
        then(fileStorageUploader).should(never()).upload(any(byte[].class), anyString(), anyString());
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
                .isInstanceOf(WorkFileStorageException.class)
                .hasMessage("작품 업로드 처리에 실패했습니다.")
                .hasCauseInstanceOf(IllegalStateException.class);

        then(fileStorageUploader).should().upload(eq(thumbnailFile), eq("thumbnail"));
        then(fileStorageUploader).should(times(3)).upload(any(byte[].class), anyString(), anyString());
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

    @DisplayName("페이징 처리된 작품 목록 조회 : 데이터 존재")
    @Test
    void test_getWorkSummaries() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        WorkSummary workSummary = new WorkSummary(
                1L,
                "/uploads/thumbnail/work-list.png",
                "목록 조회용 작품",
                "COMPLETED",
                true
        );
        Page<WorkSummary> expected = new PageImpl<>(List.of(workSummary), pageRequest, 1);

        given(workRepository.findAllWithPagination(eq(pageRequest))).willReturn(expected);

        Page<WorkSummary> actual = workService.getWorkSummaries(pageRequest);

        assertThat(actual).isSameAs(expected);
        assertThat(actual.getTotalElements()).isEqualTo(1);
        assertThat(actual.getContent()).containsExactly(workSummary);
        then(workRepository).should().findAllWithPagination(eq(pageRequest));
    }

    @DisplayName("페이징 처리된 작품 목록 조회 : 데이터 없음")
    @Test
    void test_getWorkSummaries_empty() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<WorkSummary> expected = Page.empty(pageRequest);

        given(workRepository.findAllWithPagination(eq(pageRequest))).willReturn(expected);

        Page<WorkSummary> actual = workService.getWorkSummaries(pageRequest);

        assertThat(actual).isSameAs(expected);
        assertThat(actual.getTotalElements()).isZero();
        assertThat(actual.getContent()).isEmpty();
        then(workRepository).should().findAllWithPagination(eq(pageRequest));
    }

    @DisplayName("특정 작품 조회")
    @Test
    void test_getWorkInfo() {
        long workId = 1L;
        long totalEpisodes = 12L;
        ThumbnailImageEntity thumbnailImage = TestDataGenerator.thumbnailImageEntity("/uploads/thumbnail/work-detail.png", 1024);
        WorkEntity workEntity = TestDataGenerator.workEntity(workId, "상세 조회용 작품", thumbnailImage);

        given(workRepository.findById(eq(workId))).willReturn(Optional.of(workEntity));
        given(episodeRepository.countAllByWork_Id(eq(workId))).willReturn(totalEpisodes);

        WorkInfo actual = workService.getWorkInfo(workId);

        assertThat(actual.id()).isEqualTo(workId);
        assertThat(actual.thumbnailUrl()).isEqualTo(thumbnailImage.getFileUrl());
        assertThat(actual.serialState()).isEqualTo(workEntity.getSerialState());
        assertThat(actual.title()).isEqualTo(workEntity.getTitle());
        assertThat(actual.description()).isEqualTo(workEntity.getDescription());
        assertThat(actual.episodeTotalSize()).isEqualTo(totalEpisodes);
        assertThat(actual.lastUpdateDate()).isEqualTo(workEntity.getModifiedAt().toLocalDate());

        then(workRepository).should().findById(eq(workId));
        then(episodeRepository).should().countAllByWork_Id(eq(workId));
    }

    @DisplayName("특정 작품 조회 : 조회 실패")
    @Test
    void test_getWorkInfo_fail_when_notFound() {
        long workId = 999L;

        given(workRepository.findById(eq(workId))).willReturn(Optional.empty());

        assertThatThrownBy(() -> workService.getWorkInfo(workId))
                .isInstanceOf(EntityNotFoundException.class);

        then(workRepository).should().findById(eq(workId));
        then(episodeRepository).shouldHaveNoInteractions();
    }

    @DisplayName("특정 작품 내 에피소드 정보 목록 조회")
    @Test
    void test_getEpisodeInfosByWork() {
        long workId = 1L;
        PageRequest pageRequest = PageRequest.of(0, 5);
        LocalDate lastUpdateDate = LocalDate.now();
        ThumbnailImageEntity thumbnailImage = TestDataGenerator.thumbnailImageEntity("/uploads/thumbnail/episode-1.png", 1024);
        WorkEntity workEntity = TestDataGenerator.workEntity(workId, "에피소드 목록 조회용 작품", thumbnailImage);
        EpisodeEntity episodeEntity = TestDataGenerator.episodeEntity(1L, workEntity, 1, thumbnailImage);
        EpisodeInfo episodeInfo = new EpisodeInfo(
                episodeEntity.getId(),
                workId,
                thumbnailImage.getFileUrl(),
                episodeEntity.getEpisodeNo(),
                12,
                lastUpdateDate
        );
        Page<EpisodeInfo> expected = new PageImpl<>(List.of(episodeInfo), pageRequest, 1);

        given(episodeRepository.findAllByWorkId(eq(workId), eq(pageRequest))).willReturn(expected);

        Page<EpisodeInfo> actual = workService.getEpisodeInfosByWork(workId, pageRequest);

        assertThat(actual).isSameAs(expected);
        assertThat(actual.getTotalElements()).isEqualTo(1);
        assertThat(actual.getContent()).containsExactly(episodeInfo);
        assertThat(actual.getContent().get(0).workId()).isEqualTo(workId);
        assertThat(actual.getContent().get(0).episodeThumbnailUrl()).isEqualTo(thumbnailImage.getFileUrl());
        assertThat(actual.getContent().get(0).episodeNo()).isEqualTo(episodeEntity.getEpisodeNo());
        then(episodeRepository).should().findAllByWorkId(eq(workId), eq(pageRequest));
    }

    @DisplayName("특정 작품 내 에피소드 정보 목록 조회 : 조회 실패")
    @Test
    void test_getEpisodeInfosByWork_not_found() {
        long workId = 999L;
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<EpisodeInfo> expected = Page.empty(pageRequest);

        given(episodeRepository.findAllByWorkId(eq(workId), eq(pageRequest))).willReturn(expected);

        Page<EpisodeInfo> actual = workService.getEpisodeInfosByWork(workId, pageRequest);

        assertThat(actual).isSameAs(expected);
        assertThat(actual.getTotalElements()).isZero();
        assertThat(actual.getContent()).isEmpty();
        then(episodeRepository).should().findAllByWorkId(eq(workId), eq(pageRequest));
    }

    @DisplayName("특정 작품 데이터 삭제 : 성공")
    @Test
    void test_clearWorkData() {
        long workId = 444L;
        ThumbnailImageEntity workThumbnail = TestDataGenerator.thumbnailImageEntity("/uploads/thumbnail/work.png", 1024);
        WorkEntity workEntity = TestDataGenerator.workEntity(workId, "삭제 대상 작품", workThumbnail);
        ThumbnailImageEntity episodeThumbnail1 = TestDataGenerator.thumbnailImageEntity("/uploads/thumbnail/episode-1.png", 512);
        ThumbnailImageEntity episodeThumbnail2 = TestDataGenerator.thumbnailImageEntity("/uploads/thumbnail/episode-2.png", 512);
        EpisodeEntity episode1 = TestDataGenerator.episodeEntity(1L, workEntity, 1, episodeThumbnail1);
        EpisodeEntity episode2 = TestDataGenerator.episodeEntity(2L, workEntity, 2, episodeThumbnail2);
        List<EpisodeEntity> episodes = List.of(episode1, episode2);
        long episodeImageCount = 30L;
        ArgumentCaptor<List<ThumbnailImageEntity>> thumbnailCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Iterable> episodeCaptor = ArgumentCaptor.forClass(Iterable.class);

        given(workRepository.findById(eq(workId))).willReturn(Optional.of(workEntity));
        given(episodeRepository.findAllByWork_Id(eq(workId))).willReturn(episodes);
        given(episodeImageRepository.deleteAllByEpisodeIdIn(eq(List.of(episode1.getId(), episode2.getId())))).willReturn(episodeImageCount);

        workService.clearWorkData(workId);

        then(workRepository).should().findById(eq(workId));
        then(fileStorageUploader).should().delete(eq(workThumbnail.getFileUrl()));
        then(fileStorageUploader).should().delete(eq("works/" + workId + "/"));
        then(episodeImageRepository).should().deleteAllByEpisodeIdIn(eq(List.of(episode1.getId(), episode2.getId())));
        then(episodeRepository).should().deleteAllInBatch(episodeCaptor.capture());
        then(thumbnailImageRepository).should().deleteAllInBatch(thumbnailCaptor.capture());
        then(thumbnailImageRepository).should().delete(eq(workThumbnail));
        then(workRepository).should().delete(eq(workEntity));

        assertThat(thumbnailCaptor.getValue()).containsAll(List.of(episodeThumbnail1, episodeThumbnail2));
        assertThat(toList((Iterable<EpisodeEntity>) episodeCaptor.getValue())).containsExactlyElementsOf(episodes);
    }

    @DisplayName("특정 작품 데이터 삭제 : 작품 조회 실패")
    @Test
    void test_clearWorkData_not_found_work() {
        long workId = 444L;

        given(workRepository.findById(eq(workId))).willReturn(Optional.empty());

        assertThatThrownBy(() -> workService.clearWorkData(workId))
                .isInstanceOf(EntityNotFoundException.class);

        then(workRepository).should().findById(eq(workId));
        then(fileStorageUploader).shouldHaveNoInteractions();
        then(thumbnailImageRepository).shouldHaveNoInteractions();
        then(episodeRepository).shouldHaveNoInteractions();
        then(episodeImageRepository).shouldHaveNoInteractions();
    }

    @DisplayName("특정 작품 데이터 삭제 : 에피소드 목록 조회 결과 없음")
    @Test
    void test_clearWorkData_empty_episode_list() {
        long workId = 444L;
        ThumbnailImageEntity workThumbnail = TestDataGenerator.thumbnailImageEntity("/uploads/thumbnail/work.png", 1024);
        WorkEntity workEntity = TestDataGenerator.workEntity(workId, "에피소드 없는 작품", workThumbnail);
        ArgumentCaptor<Iterable> episodeCaptor = ArgumentCaptor.forClass(Iterable.class);

        given(workRepository.findById(eq(workId))).willReturn(Optional.of(workEntity));
        given(episodeRepository.findAllByWork_Id(eq(workId))).willReturn(List.of());

        workService.clearWorkData(workId);

        then(workRepository).should().findById(eq(workId));
        then(thumbnailImageRepository).should().delete(eq(workThumbnail));
        then(episodeImageRepository).shouldHaveNoInteractions();
        then(episodeRepository).should().deleteAllInBatch(episodeCaptor.capture());
        then(workRepository).should().delete(eq(workEntity));
        then(fileStorageUploader).should().delete(eq(workThumbnail.getFileUrl()));
        then(fileStorageUploader).should().delete(eq("works/" + workId + "/"));

        assertThat(toList((Iterable<EpisodeEntity>) episodeCaptor.getValue())).isEmpty();
    }

    @DisplayName("특정 작품 데이터 삭제 : 스토리지 내 파일 삭제 실패")
    @Test
    void test_clearWorkData_failed_clear_storage_files() {
        long workId = 444L;
        WorkEntity workEntity = TestDataGenerator.workEntity(workId, "삭제 실패 대상 작품", null);
        ThumbnailImageEntity episodeThumbnail = TestDataGenerator.thumbnailImageEntity("/uploads/thumbnail/episode-1.png", 512);
        EpisodeEntity episode = TestDataGenerator.episodeEntity(1L, workEntity, 1, episodeThumbnail);

        given(workRepository.findById(eq(workId))).willReturn(Optional.of(workEntity));
        given(episodeRepository.findAllByWork_Id(eq(workId))).willReturn(List.of(episode));
        willAnswer(invocation -> {
            String filePath = invocation.getArgument(0, String.class);
            if (("works/" + workId + "/").equals(filePath)) {
                throw new IllegalStateException("storage delete failed");
            }
            return null;
        }).given(fileStorageUploader).delete(anyString());

        assertThatCode(() -> workService.clearWorkData(workId))
                .isInstanceOf(IllegalStateException.class);

        then(workRepository).should().findById(eq(workId));
        then(episodeImageRepository).should().deleteAllByEpisodeIdIn(anyList());
        then(episodeRepository).should().deleteAllInBatch(anyIterable());
        then(thumbnailImageRepository).should().deleteAllInBatch(anyIterable());
        then(workRepository).should().delete(any(WorkEntity.class));
        then(fileStorageUploader).should().delete(eq("works/" + workId + "/"));
    }

    @DisplayName("특정 작품 정보 갱신")
    @Test
    void test_updateWork() {
        long workId = 1L;
        WorkUpdateRequest updateRequest = new WorkUpdateRequest(
                "update-title",
                "description",
                "COMPLETED",
                true
        );
        WorkEntity expected = TestDataGenerator.workEntity(workId, "test-title", null);

        given(workRepository.findById(eq(workId))).willReturn(Optional.of(expected));

        workService.updateWork(workId, updateRequest);

        assertThat(expected.getTitle()).isEqualTo(updateRequest.title());
        assertThat(expected.getDescription()).isEqualTo(updateRequest.description());
        assertThat(expected.getSerialState()).isEqualTo(updateRequest.serialState());
        assertThat(expected.isVisibility()).isEqualTo(updateRequest.visibility());

        then(workRepository).should().findById(eq(workId));
    }

    @DisplayName("특정 작품 정보 갱신 - 작품 조회 실패")
    @Test
    void test_updateWork_not_found_work() {
        long workId = 999L;
        WorkUpdateRequest updateRequest = new WorkUpdateRequest(
                "update-title",
                "description",
                "COMPLETED",
                true
        );

        given(workRepository.findById(eq(workId))).willReturn(Optional.empty());

        assertThatThrownBy(() -> workService.updateWork(workId, updateRequest))
                .isInstanceOf(EntityNotFoundException.class);

        then(workRepository).should().findById(eq(workId));
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
        given(fileStorageUploader.upload(any(byte[].class), anyString(), anyString()))
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
