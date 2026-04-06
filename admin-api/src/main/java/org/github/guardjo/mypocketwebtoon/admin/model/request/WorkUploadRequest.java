package org.github.guardjo.mypocketwebtoon.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/* 작품 업로드 요청 VO */
public record WorkUploadRequest(
        @Schema(name = "작품명", example = "마음의소리")
        @NotBlank
        String title,

        @Schema(name = "작품설명", example = "작품 내용 설명", nullable = true)
        String description,

        @Schema(name = "연재상태", example = "COMPLETED")
        @NotBlank
        String serialState,

        @Schema(name = "노출 여부", defaultValue = "false")
        boolean visibility,

        @Schema(name = "작품 썸네일 이미지 파일", example = "xxx.jpg", nullable = true)
        MultipartFile thumbnailFile,

        @Schema(name = "작품 회차별 리소스 압축 파일", example = "xxx.tar")
        @NotNull
        MultipartFile episodeFile
) {
}
