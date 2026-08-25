package org.github.guardjo.mypocketwebtoon.admin.repository.querydsl.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.QUserInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserMetricCountInfo;
import org.github.guardjo.mypocketwebtoon.admin.repository.querydsl.UserMetricRepository;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.time.LocalDate;

public class UserMetricRepositoryImpl extends QuerydslRepositorySupport implements UserMetricRepository {
    public UserMetricRepositoryImpl() {
        super(QUserInfoEntity.class);
    }

    @Override
    public UserMetricCountInfo calculateUserManagementMetric(LocalDate targetDate) {
        QUserInfoEntity qUserInfoEntity = QUserInfoEntity.userInfoEntity;

        NumberExpression<Long> activeCount = calculateUserCount(qUserInfoEntity.activate.isTrue());
        NumberExpression<Long> pendingCount = calculateUserCount(qUserInfoEntity.activate.isFalse());
        NumberExpression<Long> currentMonthCount = calculateUserCount(qUserInfoEntity.createdAt.goe(targetDate.atStartOfDay()));
        NumberExpression<Long> lastMonthCount = calculateUserCount(
                qUserInfoEntity.createdAt.goe(targetDate.minusMonths(1).atStartOfDay())
                        .and(qUserInfoEntity.createdAt.lt(targetDate.atStartOfDay()))
        );

        return from(qUserInfoEntity)
                .select(Projections.constructor(
                        UserMetricCountInfo.class,
                        qUserInfoEntity.count(),
                        activeCount,
                        pendingCount,
                        currentMonthCount,
                        lastMonthCount
                ))
                .fetchOne();
    }

    private NumberExpression<Long> calculateUserCount(Predicate predicate) {
        return new CaseBuilder()
                .when(predicate)
                .then(1L)
                .otherwise(0L)
                .sum()
                .coalesce(0L);
    }
}
