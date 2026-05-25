package org.github.guardjo.mypocketwebtoon.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/* 작품 정보 업데이트 요청 VO */
public record WorkUpdateRequest(
        @Schema(description = "작품명", example = "마음의소리")
        @NotBlank
        String title,

        @Schema(description = "작품설명", example = "작품 내용 설명", nullable = true)
        String description,

        @Schema(description = "연재상태", example = "COMPLETED")
        @NotBlank
        String serialState,

        @Schema(description = "노출 여부", defaultValue = "false")
        boolean visibility
) {
}
