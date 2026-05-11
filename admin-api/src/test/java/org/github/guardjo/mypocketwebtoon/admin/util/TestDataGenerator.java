package org.github.guardjo.mypocketwebtoon.admin.util;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.*;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.EpisodeInfo;

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

    public static WorkEntity workEntity(Long id, String title, ThumbnailImageEntity thumbnailImage) {
        return WorkEntity.builder()
                .id(id)
                .title(title)
                .description("test-description")
                .serialState("COMPLETED")
                .thumbnailImage(thumbnailImage)
                .visibility(true)
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

    public static EpisodeEntity episodeEntity(Long id, WorkEntity work, int episodeNo, ThumbnailImageEntity thumbnailImage) {
        return EpisodeEntity.builder()
                .id(id)
                .work(work)
                .episodeNo(episodeNo)
                .thumbnailImage(thumbnailImage)
                .likeCount(0)
                .viewCount(0)
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

    public static EpisodeImageEntity episodeImage(EpisodeEntity episode, int sortOrder, String fileUrl, long fileSize) {
        return EpisodeImageEntity.builder()
                .episode(episode)
                .sortOrder(sortOrder)
                .fileUrl(fileUrl)
                .fileSize(fileSize)
                .build();
    }

    public static EpisodeInfo episodeInfo(EpisodeEntity episode, int episodeImageTotalCount) {
        ThumbnailImageEntity thumbnailImage = episode.getThumbnailImage();

        return new EpisodeInfo(
                episode.getId(),
                episode.getWork().getId(),
                thumbnailImage == null ? null : thumbnailImage.getFileUrl(),
                episode.getEpisodeNo(),
                episodeImageTotalCount,
                episode.getModifiedAt().toLocalDate()
        );
    }
}
