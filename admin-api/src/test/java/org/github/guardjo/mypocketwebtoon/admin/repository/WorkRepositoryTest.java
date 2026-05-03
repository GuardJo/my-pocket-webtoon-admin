package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.WorkEntity;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkRepositoryTest {
    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private ThumbnailImageRepository thumbnailImageRepository;

    @DisplayName("작품 정보 정상 저장")
    @Test
    void test_save() {
        ThumbnailImageEntity thumbnailImage = thumbnailImageRepository.saveAndFlush(
                TestDataGenerator.thumbnailImageEntity("https://cdn.example.com/thumbnail/work-save.png", 1024)
        );
        WorkEntity expected = TestDataGenerator.workEntity("테스트 작품", thumbnailImage);

        WorkEntity actual = workRepository.saveAndFlush(expected);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual).usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "modifiedAt")
                .isEqualTo(expected);
    }

    @DisplayName("작품 ID로 조회")
    @Test
    void test_findById() {
        ThumbnailImageEntity thumbnailImage = thumbnailImageRepository.saveAndFlush(
                TestDataGenerator.thumbnailImageEntity("https://cdn.example.com/thumbnail/work-find.png", 2048)
        );
        WorkEntity expected = workRepository.saveAndFlush(
                TestDataGenerator.workEntity("조회용 작품", thumbnailImage)
        );

        WorkEntity actual = workRepository.findById(expected.getId())
                .orElseThrow();

        assertThat(actual).usingRecursiveComparison()
                .ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(expected);
    }
}
