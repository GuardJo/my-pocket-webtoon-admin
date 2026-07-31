package org.github.guardjo.mypocketwebtoon.admin.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/* 회원 등록 요청 VO */
public record UserCreateRequest(
        @Schema(description = "회원 아이디", example = "tester")
        @NotBlank(message = "아이디를 입력해주세요.")
        String id,

        @Schema(description = "회원명", example = "김아무개")
        @NotBlank(message = "이름을 입력해주세요.")
        String name,

        @Schema(description = "회원 닉네임", example = "tester01")
        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickname,

        @Schema(description = "비밀번호", example = "password1!")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자 사이로 입력해주세요."
        )
        String password,

        @Schema(description = "생년월일", example = "1990-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthYmd
) {
}
