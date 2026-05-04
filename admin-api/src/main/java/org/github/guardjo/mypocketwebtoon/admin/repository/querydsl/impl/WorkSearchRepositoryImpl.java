package org.github.guardjo.mypocketwebtoon.admin.repository.querydsl.impl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.PathBuilder;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.QThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.QWorkEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.WorkEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkSummary;
import org.github.guardjo.mypocketwebtoon.admin.repository.querydsl.WorkSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Objects;

public class WorkSearchRepositoryImpl extends QuerydslRepositorySupport implements WorkSearchRepository {
    public WorkSearchRepositoryImpl() {
        super(QWorkEntity.class);
    }

    @Override
    public Page<WorkSummary> findAllWithPagination(Pageable pageable) {
        QWorkEntity qWorkEntity = QWorkEntity.workEntity;
        QThumbnailImageEntity qThumbnailImageEntity = QThumbnailImageEntity.thumbnailImageEntity;

        Long totalCount = from(qWorkEntity)
                .select(qWorkEntity.count())
                .fetchOne();

        if (Objects.isNull(totalCount) || totalCount == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<WorkSummary> content = from(qWorkEntity)
                .leftJoin(qWorkEntity.thumbnailImage, qThumbnailImageEntity)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .select(Projections.constructor(WorkSummary.class,
                        qWorkEntity.id,
                        qThumbnailImageEntity.fileUrl,
                        qWorkEntity.title,
                        qWorkEntity.serialState,
                        qWorkEntity.visibility))
                .orderBy(getWorkOrderSpecifier(pageable))
                .fetch();

        return new PageImpl<>(content, pageable, totalCount);
    }

    /*
    work Entity에 대한 정렬 옵션 추출
     */
    private OrderSpecifier<?>[] getWorkOrderSpecifier(Pageable pageable) {
        return pageable.getSort().stream()
                .map((order) -> {
                    PathBuilder<WorkEntity> pathBuilder = new PathBuilder<>(WorkEntity.class, "workEntity");
                    ComparableExpressionBase<?> expressionBase = pathBuilder.getComparable(order.getProperty(), Comparable.class);
                    return new OrderSpecifier<>(order.isAscending() ? Order.ASC : Order.DESC, expressionBase);
                })
                .toArray(OrderSpecifier[]::new);
    }
}
