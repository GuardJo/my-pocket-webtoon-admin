package org.github.guardjo.mypocketwebtoon.admin.util;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminRoleEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.WorkEntity;

public class TestDataGenerator {
    private TestDataGenerator() {
        // Constant Class
    }

    public static AdminRoleEntity adminRoleEntity(String id) {
        return AdminRoleEntity.builder()
                .id(id)
                .description("test")
                .activate(true)
                .build();
    }

    public static AdminInfoEntity adminInfoEntity(String id, String name) {
        AdminRoleEntity roleEntity = adminRoleEntity("TEST");

        return AdminInfoEntity.builder()
                .id(id)
                .password("{noop}test-password")
                .name(name)
                .activate(true)
                .role(roleEntity)
                .build();
    }

    public static ThumbnailImageEntity thumbnailImageEntity(String fileUrl, int fileSize) {
        return ThumbnailImageEntity.builder()
                .fileUrl(fileUrl)
                .fileSize(fileSize)
                .build();
    }

    public static WorkEntity workEntity(String title, ThumbnailImageEntity thumbnailImage) {
        return WorkEntity.builder()
                .title(title)
                .description("test-description")
                .serialState("COMPLETED")
                .thumbnailImage(thumbnailImage)
                .visibility(true)
                .build();
    }

    public static EpisodeEntity episodeEntity(WorkEntity work, int episodeNo, ThumbnailImageEntity thumbnailImage) {
        return EpisodeEntity.builder()
                .work(work)
                .episodeNo(episodeNo)
                .thumbnailImage(thumbnailImage)
                .likeCount(0)
                .viewCount(0)
                .build();
    }
}
