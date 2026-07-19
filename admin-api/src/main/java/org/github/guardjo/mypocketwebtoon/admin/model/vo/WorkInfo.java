package org.github.guardjo.mypocketwebtoon.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/*
작품 정보
 */
public record WorkInfo(
        @Schema(description = "작품 식별키", example = "999")
        Long id,

        @Schema(description = "작품 대표썸네일 이미지 URL", example = "https://cdn.com/thumnbnail.jpg")
        String thumbnailUrl,

        @Schema(description = "작품 연재상태", example = "COMPLETED")
        String serialState,

        @Schema(description = "작품 공개여부", example = "true")
        boolean visibility,

        @Schema(description = "작품명", example = "웹툰명")
        String title,

        @Schema(description = "작품 설명", example = "설명")
        String description,

        @Schema(description = "회차 수", example = "100")
        int episodeTotalSize,

        @JsonFormat(pattern = "yyyy.MM.dd")
        @Schema(description = "마지막 업데이트 일자", example = "2026.05.05")
        LocalDate lastUpdateDate
) {
}
