package org.github.guardjo.mypocketwebtoon.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관리 회원 매트릭 정보
 */
public record UserManagementMetric(
        @Schema(description = "전체 회원 수", example = "100")
        long totalUsers,

        @Schema(description = "활성 회원 수", example = "50")
        long activateUsers,

        @Schema(description = "승인대기 회원 수", example = "50")
        long pendingUsers,

        @Schema(description = "유지율", example = "50.0")
        float retentionRate,

        @Schema(description = "전달 대비 회원 증가량", example = "10")
        long monthlyMemberGrowth
) {
}
