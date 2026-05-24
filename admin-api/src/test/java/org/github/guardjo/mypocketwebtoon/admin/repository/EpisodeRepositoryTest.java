package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.WorkEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.EpisodeInfo;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EpisodeRepositoryTest {
    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private ThumbnailImageRepository thumbnailImageRepository;

    @Autowired
    private EpisodeImageRepository episodeImageRepository;

    @DisplayName("신규 episode 저장 성공")
    @Test
    void test_save() {
        WorkEntity savedWork = saveWork("에피소드 저장용 작품", "https://cdn.example.com/thumbnail/work-episode-save.png");
        ThumbnailImageEntity savedEpisodeThumbnail = saveThumbnailImage("https://cdn.example.com/thumbnail/episode-save.png");
        EpisodeEntity expected = TestDataGenerator.episodeEntity(savedWork, 1, savedEpisodeThumbnail);

        EpisodeEntity actual = episodeRepository.saveAndFlush(expected);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual).usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "modifiedAt")
                .ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(expected);
    }

    @DisplayName("episode 저장 간 기존 저장 요소와 값 충돌")
    @Test
    void test_save_duplicateEpisodeNoInSameWork() {
        WorkEntity savedWork = saveWork("중복 검사용 작품", "https://cdn.example.com/thumbnail/work-episode-duplicate.png");
        ThumbnailImageEntity firstEpisodeThumbnail = saveThumbnailImage("https://cdn.example.com/thumbnail/episode-duplicate-1.png");
        ThumbnailImageEntity duplicatedEpisodeThumbnail = saveThumbnailImage("https://cdn.example.com/thumbnail/episode-duplicate-2.png");

        episodeRepository.saveAndFlush(TestDataGenerator.episodeEntity(savedWork, 1, firstEpisodeThumbnail));
        EpisodeEntity duplicatedEpisode = TestDataGenerator.episodeEntity(savedWork, 1, duplicatedEpisodeThumbnail);

        assertThatCode(() -> episodeRepository.saveAndFlush(duplicatedEpisode))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("episode 삭제")
    @Test
    void test_delete() {
        WorkEntity savedWork = saveWork("에피소드 삭제용 작품", "https://cdn.example.com/thumbnail/work-episode-delete.png");
        ThumbnailImageEntity savedEpisodeThumbnail = saveThumbnailImage("https://cdn.example.com/thumbnail/episode-delete.png");
        EpisodeEntity savedEpisode = episodeRepository.saveAndFlush(
                TestDataGenerator.episodeEntity(savedWork, 1, savedEpisodeThumbnail)
        );

        episodeRepository.delete(savedEpisode);
        episodeRepository.flush();

        Optional<EpisodeEntity> actual = episodeRepository.findById(savedEpisode.getId());

        assertThat(actual.isEmpty()).isTrue();
    }

    @DisplayName("특정 work에 대한 episode 개수 조회")
    @Test
    void test_countAllByWork_Id() {
        WorkEntity saveWork = saveWork("에피소드 수량 조회용 작품", "https://cdn.example.com/thumbnail/episode-count.png");
        long expected = 10L;

        for (int i = 0; i < expected; i++) {
            episodeRepository.saveAndFlush(
                    TestDataGenerator.episodeEntity(saveWork, i, null)
            );
        }

        long actual = episodeRepository.countAllByWork_Id(saveWork.getId());

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("특정 work에 대한 episode 목록 조회")
    @Test
    void test_findAllByWorkId() {
        int pageNumber = 0;
        int pageSize = 5;
        int totalCount = 10;
        int episodeCount = 12;
        List<EpisodeEntity> episodeEntityList = new ArrayList<>();

        WorkEntity work = saveWork("에피소드 목록 조회용 작품", "https://cdn.example.com/thumbnail/work-image.png");

        for (int i = 1; i < totalCount + 1; i++) {
            ThumbnailImageEntity thumbnailImageEntity = TestDataGenerator.thumbnailImageEntity("https://cdn.example.com/thumbnail/episode" + i + ".jpg", 1024);
            thumbnailImageRepository.saveAndFlush(thumbnailImageEntity);
            EpisodeEntity episodeEntity = TestDataGenerator.episodeEntity(work, i, thumbnailImageEntity);
            episodeRepository.saveAndFlush(episodeEntity);
            List<EpisodeImageEntity> episodeImageEntities = new ArrayList<>();
            for (int j = 0; j < episodeCount; j++) {
                episodeImageEntities.add(TestDataGenerator.episodeImage(episodeEntity, j + 1, "https://cnd.example.com/works/" + episodeEntity.getId() + "/episode-image-" + j + ".png", 1024));
            }
            episodeImageRepository.saveAllAndFlush(episodeImageEntities);

            episodeEntityList.add(episodeEntity);
        }
        episodeRepository.saveAllAndFlush(episodeEntityList);

        Page<EpisodeInfo> episodeInfos = episodeRepository.findAllByWorkId(work.getId(), PageRequest.of(pageNumber, pageSize));

        assertThat(episodeInfos.getTotalElements()).isEqualTo(totalCount);
        assertThat(episodeInfos.getContent().size()).isEqualTo(pageSize);

        for (int i = 0; i < episodeInfos.getContent().size(); i++) {
            EpisodeInfo actual = episodeInfos.getContent().get(i);
            EpisodeEntity expected = episodeEntityList.get(i);

            assertThat(actual.id()).isEqualTo(expected.getId());
            assertThat(actual.workId()).isEqualTo(expected.getWork().getId());
            assertThat(actual.episodeThumbnailUrl()).isEqualTo(expected.getThumbnailImage().getFileUrl());
            assertThat(actual.episodeNo()).isEqualTo(expected.getEpisodeNo());
            assertThat(actual.episodeImageTotalCount()).isEqualTo(episodeCount);
            assertThat(actual.lastUpdateDate()).isEqualTo(expected.getModifiedAt().toLocalDate());
        }
    }

    @DisplayName("work_id에 해당하는 episode Entity 목록 반환")
    @Test
    void test_findAllByWork_Id() {
        WorkEntity savedWork = saveWork("에피소드 저장용 작품", "https://cdn.example.com/thumbnail/work-episode.png");
        WorkEntity savedWork2 = saveWork("에피소드 저장용 작품2", "https://cdn.example.com/thumbnail/work-episode2.png");

        List<EpisodeEntity> expected = List.of(
                TestDataGenerator.episodeEntity(savedWork, 1, saveThumbnailImage("https://cdn.example.com/thumbnail/episode1.png")),
                TestDataGenerator.episodeEntity(savedWork, 2, saveThumbnailImage("https://cdn.example.com/thumbnail/episode2.png")),
                TestDataGenerator.episodeEntity(savedWork, 3, saveThumbnailImage("https://cdn.example.com/thumbnail/episode3.png"))
        );

        List<EpisodeEntity> savedEpisodes = List.of(
                TestDataGenerator.episodeEntity(savedWork2, 1, saveThumbnailImage("https://cdn.example.com/thumbnail/episode11.png")),
                TestDataGenerator.episodeEntity(savedWork2, 2, saveThumbnailImage("https://cdn.example.com/thumbnail/episode22.png")),
                TestDataGenerator.episodeEntity(savedWork2, 3, saveThumbnailImage("https://cdn.example.com/thumbnail/episode33.png"))
        );


        episodeRepository.saveAllAndFlush(expected);
        episodeRepository.saveAllAndFlush(savedEpisodes);

        List<EpisodeEntity> actual = episodeRepository.findAllByWork_Id(savedWork.getId());

        assertThat(actual).isEqualTo(expected);
    }

    private WorkEntity saveWork(String title, String thumbnailUrl) {
        ThumbnailImageEntity savedThumbnail = saveThumbnailImage(thumbnailUrl);
        return workRepository.saveAndFlush(TestDataGenerator.workEntity(title, savedThumbnail));
    }

    private ThumbnailImageEntity saveThumbnailImage(String fileUrl) {
        return thumbnailImageRepository.saveAndFlush(
                TestDataGenerator.thumbnailImageEntity(fileUrl, 1024)
        );
    }
}
