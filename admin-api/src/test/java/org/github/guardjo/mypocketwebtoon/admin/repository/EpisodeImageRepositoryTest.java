package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeImageEntity;
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
class EpisodeImageRepositoryTest {
    @Autowired
    private EpisodeImageRepository episodeImageRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private ThumbnailImageRepository thumbnailImageRepository;

    @DisplayName("에피소드 이미지 정상 저장")
    @Test
    void test_save() {
        EpisodeEntity savedEpisode = saveEpisode(
                "에피소드 이미지 저장용 작품",
                "https://cdn.example.com/thumbnail/work-episode-image-save.png",
                1,
                "https://cdn.example.com/thumbnail/episode-image-save.png"
        );
        EpisodeImageEntity expected = TestDataGenerator.episodeImage(savedEpisode, 1, "https://cdn.example.com/episode/image-save-1.png", 1024);

        EpisodeImageEntity actual = episodeImageRepository.saveAndFlush(expected);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual).usingRecursiveComparison()
                .ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(expected);
    }

    @DisplayName("동일 episode 내 중복 sort_order 저장 시 예외 발생")
    @Test
    void test_save_duplicateSortOrderInSameEpisode() {
        EpisodeEntity savedEpisode = saveEpisode(
                "에피소드 이미지 중복 검사용 작품",
                "https://cdn.example.com/thumbnail/work-episode-image-duplicate.png",
                1,
                "https://cdn.example.com/thumbnail/episode-image-duplicate.png"
        );

        episodeImageRepository.saveAndFlush(
                TestDataGenerator.episodeImage(savedEpisode, 1, "https://cdn.example.com/episode/image-duplicate-1.png", 1024)
        );

        EpisodeImageEntity duplicatedEpisodeImage = TestDataGenerator.episodeImage(
                savedEpisode,
                1,
                "https://cdn.example.com/episode/image-duplicate-2.png",
                2048
        );

        assertThatCode(() -> episodeImageRepository.saveAndFlush(duplicatedEpisodeImage))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("에피소드 이미지 삭제")
    @Test
    void test_delete() {
        EpisodeEntity savedEpisode = saveEpisode(
                "에피소드 이미지 삭제용 작품",
                "https://cdn.example.com/thumbnail/work-episode-image-delete.png",
                1,
                "https://cdn.example.com/thumbnail/episode-image-delete.png"
        );
        EpisodeImageEntity savedEpisodeImage = episodeImageRepository.saveAndFlush(
                TestDataGenerator.episodeImage(savedEpisode, 1, "https://cdn.example.com/episode/image-delete-1.png", 1024)
        );

        episodeImageRepository.delete(savedEpisodeImage);
        episodeImageRepository.flush();

        Optional<EpisodeImageEntity> actual = episodeImageRepository.findById(savedEpisodeImage.getId());

        assertThat(actual.isEmpty()).isTrue();
    }

    private EpisodeEntity saveEpisode(String title, String workThumbnailUrl, int episodeNo, String episodeThumbnailUrl) {
        WorkEntity savedWork = saveWork(title, workThumbnailUrl);
        ThumbnailImageEntity savedEpisodeThumbnail = saveThumbnailImage(episodeThumbnailUrl);

        return episodeRepository.saveAndFlush(
                TestDataGenerator.episodeEntity(savedWork, episodeNo, savedEpisodeThumbnail)
        );
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
