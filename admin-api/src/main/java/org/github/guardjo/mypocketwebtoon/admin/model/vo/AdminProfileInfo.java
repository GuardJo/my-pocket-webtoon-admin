package org.github.guardjo.mypocketwebtoon.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminProfileInfo(
        @Schema(description = "관리자 아이디", example = "tester")
        String id,

        @Schema(description = "관리자 권한명", example = "ADMIN")
        String roleName
) {
    public static AdminProfileInfo of(AdminInfo adminInfo) {
        return new AdminProfileInfo(adminInfo.id(), adminInfo.roleId());
    }
}
