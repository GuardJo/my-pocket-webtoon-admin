package org.github.guardjo.mypocketwebtoon.admin.model.vo;

public record UserMetricCountInfo(
        long totalCount,
        long activeCount,
        long pendingCount,
        long currentMonthCount,
        long lastMonthCount
) {
}
