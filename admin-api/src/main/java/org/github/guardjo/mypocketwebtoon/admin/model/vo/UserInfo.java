package org.github.guardjo.mypocketwebtoon.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.UserInfoEntity;

import java.time.LocalDate;

/*
회원 정보
 */
public record UserInfo(
        @Schema(description = "회원아이디", example = "tester")
        String id,

        @Schema(description = "회원명", example = "테스터")
        String name,

        @Schema(description = "회원닉네임", example = "킹왕짱")
        String nickname,

        @JsonFormat(pattern = "yyyy.MM.dd")
        @Schema(description = "회원가입일자", example = "2026.05.05")
        LocalDate signupDate,

        @Schema(description = "회원활성상태", example = "true")
        boolean activate
) {
    public static UserInfo of(UserInfoEntity entity) {
        return new UserInfo(
                entity.getId(),
                entity.getName(),
                entity.getNickname(),
                entity.getCreatedAt().toLocalDate(),
                entity.isActivate()
        );
    }
}
