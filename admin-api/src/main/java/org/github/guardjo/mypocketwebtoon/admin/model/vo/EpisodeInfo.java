package org.github.guardjo.mypocketwebtoon.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/*
에피소드 정보
 */
public record EpisodeInfo(
        @Schema(description = "에피소드 식별키", example = "999")
        Long id,

        @Schema(description = "작품 식별키", example = "100")
        Long workId,

        @Schema(description = "에피소드 썸네일 URL", example = "https://cdn.example.com/episode-thumbnail.jpg")
        String episodeThumbnailUrl,

        @Schema(description = "에피소드 회차", example = "1")
        int episodeNo,

        @Schema(description = "에피소드 내 이미지 파일 개수", example = "123")
        int episodeImageTotalCount,

        @JsonFormat(pattern = "yyyy.MM.dd")
        @Schema(description = "최근 업데이트 일자", example = "2026.05.05")
        LocalDate lastUpdateDate
) {
}
