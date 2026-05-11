package org.github.guardjo.mypocketwebtoon.admin.repository.querydsl.impl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.QEpisodeEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.QEpisodeImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.QThumbnailImageEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.EpisodeInfo;
import org.github.guardjo.mypocketwebtoon.admin.repository.querydsl.EpisodeSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;

public class EpisodeSearchRepositoryImpl extends QuerydslRepositorySupport implements EpisodeSearchRepository {
    public EpisodeSearchRepositoryImpl() {
        super(QEpisodeEntity.class);
    }

    @Override
    public Page<EpisodeInfo> findAllByWorkId(Long workId, Pageable pageable) {
        QEpisodeEntity qEpisodeEntity = QEpisodeEntity.episodeEntity;
        QThumbnailImageEntity qThumbnailImageEntity = QThumbnailImageEntity.thumbnailImageEntity;
        QEpisodeImageEntity qEpisodeImageEntity = QEpisodeImageEntity.episodeImageEntity;

        Long totalCount = from(qEpisodeEntity)
                .where(qEpisodeEntity.work.id.eq(workId))
                .select(qEpisodeEntity.count())
                .fetchOne();

        if (Objects.isNull(totalCount) || totalCount == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<EpisodeInfo> content = from(qEpisodeEntity)
                .leftJoin(qEpisodeEntity.thumbnailImage, qThumbnailImageEntity)
                .where(qEpisodeEntity.work.id.eq(workId))
                .orderBy(getEpisodeOrderSpecifier(pageable, qEpisodeEntity))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .select(Projections.constructor(
                                EpisodeInfo.class,
                                qEpisodeEntity.id,
                                qEpisodeEntity.work.id,
                                qThumbnailImageEntity.fileUrl,
                                qEpisodeEntity.episodeNo,
                                JPAExpressions.select(qEpisodeImageEntity.count().castToNum(Integer.class))
                                        .from(qEpisodeImageEntity)
                                        .where(qEpisodeImageEntity.episode.id.eq(qEpisodeEntity.id)),
                                qEpisodeEntity.modifiedAt
                        )
                )
                .fetch();

        return new PageImpl<>(content, pageable, totalCount);
    }

    private OrderSpecifier<?>[] getEpisodeOrderSpecifier(Pageable pageable, QEpisodeEntity qEpisodeEntity) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        // episodeNo 순 오름차로 정렬되도록 구성
        orderSpecifiers.add(qEpisodeEntity.episodeNo.asc());

        for (Sort.Order order : pageable.getSort()) {
            Order direction = order.isAscending() ? ASC : DESC;
            PathBuilder<EpisodeEntity> pathBuilder = new PathBuilder<>(EpisodeEntity.class, "episodeEntity");
            ComparableExpressionBase<?> expressionBase = pathBuilder.getComparable(order.getProperty(), Comparable.class);
            orderSpecifiers.add(new OrderSpecifier<>(direction, expressionBase));
        }

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }
}
