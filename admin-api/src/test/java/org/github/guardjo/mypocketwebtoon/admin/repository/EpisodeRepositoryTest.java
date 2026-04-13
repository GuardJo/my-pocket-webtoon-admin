package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.WorkEntity;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
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
