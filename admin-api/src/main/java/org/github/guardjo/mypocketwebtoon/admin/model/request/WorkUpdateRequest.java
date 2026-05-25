package org.github.guardjo.mypocketwebtoon.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* 작품 정보 업데이트 요청 VO */
public record WorkUpdateRequest(
        @Schema(description = "작품명", example = "마음의소리", maxLength = 100)
        @NotBlank
        @Size(max = 100)
        String title,

        @Schema(description = "작품설명", example = "작품 내용 설명", nullable = true, maxLength = 500)
        @Size(max = 500)
        String description,

        @Schema(description = "연재상태", example = "COMPLETED", maxLength = 10)
        @NotBlank
        @Size(max = 10)
        String serialState,

        @Schema(description = "노출 여부", defaultValue = "false")
        boolean visibility
) {
}
