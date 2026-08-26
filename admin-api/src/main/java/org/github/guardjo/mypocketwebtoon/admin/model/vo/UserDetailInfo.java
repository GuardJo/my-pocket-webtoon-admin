package org.github.guardjo.mypocketwebtoon.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/*
회원 상세 정보
 */
public record UserDetailInfo(
        @Schema(description = "회원아이디", example = "tester")
        String id,

        @Schema(description = "회원명", example = "테스터")
        String name,

        @Schema(description = "회원닉네임", example = "킹왕짱")
        String nickname,

        @JsonFormat(pattern = "yyyy.MM.dd")
        @Schema(description = "생년월일", example = "1996.05.05")
        LocalDate birthday,

        @JsonFormat(pattern = "yyyy.MM.dd")
        @Schema(description = "회원가입일자", example = "2026.05.05")
        LocalDate signupDate,

        @JsonFormat(pattern = "yyyy.MM.dd")
        @Schema(description = "수정일자", example = "2026.05.05")
        LocalDate lastUpdateDate,

        @Schema(description = "회원활성상태", example = "true")
        boolean activate,

        @Schema(description = "승인관리자 아이디", example = "admin")
        String registerAdminId
) {
}
