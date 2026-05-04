package org.github.guardjo.mypocketwebtoon.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/*
작품 기본 정보
 */
public record WorkSummary(
        @Schema(description = "작품 식별키", example = "999")
        long id,

        @Schema(description = "작품 대표썸네일 이미지 URL", example = "https://cdn.com/thumbnail.jpg")
        String thumbnailUrl,

        @Schema(description = "작품명", example = "웹툰명")
        String title,

        @Schema(description = "작품상태", example = "COMPLETED")
        String serialState,

        @Schema(description = "작품 노출 여부", example = "true")
        boolean visibility
) {
}
