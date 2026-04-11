package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ThumbnailImageRepositoryTest {
    @Autowired
    private ThumbnailImageRepository thumbnailImageRepository;

    @DisplayName("썸네일 이미지 정상 저장")
    @Test
    void test_save() {
        ThumbnailImageEntity thumbnailImage = TestDataGenerator.thumbnailImageEntity(
                "https://cdn.example.com/thumbnail/test-image.png",
                1024
        );

        ThumbnailImageEntity actual = thumbnailImageRepository.saveAndFlush(thumbnailImage);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getFileUrl()).isEqualTo(thumbnailImage.getFileUrl());
        assertThat(actual.getFileSize()).isEqualTo(thumbnailImage.getFileSize());
    }

    @DisplayName("중복된 fileUrl 저장 시 예외 발생")
    @Test
    void test_save_duplicateFileUrl() {
        String fileUrl = "https://cdn.example.com/thumbnail/duplicated-image.png";
        ThumbnailImageEntity savedThumbnailImage = TestDataGenerator.thumbnailImageEntity(fileUrl, 1024);
        ThumbnailImageEntity duplicatedThumbnailImage = TestDataGenerator.thumbnailImageEntity(fileUrl, 2048);

        thumbnailImageRepository.saveAndFlush(savedThumbnailImage);

        assertThatCode(() -> thumbnailImageRepository.saveAndFlush(duplicatedThumbnailImage))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
