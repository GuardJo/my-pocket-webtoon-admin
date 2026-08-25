package org.github.guardjo.mypocketwebtoon.admin.repository.querydsl;

import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserMetricCountInfo;

import java.time.LocalDate;

public interface UserMetricRepository {
    /**
     * 주어진 시점 기준으로 회원 매트릭 정보를 계산하여 반환한다.
     *
     * @param targetDate 기준 일시
     * @return 회원 매트릭 정보 (전체 회원 수, 유지율 등)
     */
    UserMetricCountInfo calculateUserManagementMetric(LocalDate targetDate);
}
