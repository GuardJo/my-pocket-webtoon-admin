package org.github.guardjo.mypocketwebtoon.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* 관리자 로그인 요청 VO */
public record LoginRequest(
        @Schema(description = "관리자 아이디", example = "tester")
        @NotBlank
        @Size(max = 20)
        String id,

        @Schema(description = "관리자 비밀번호", example = "평문 암호")
        @NotBlank
        String password
) {
}
