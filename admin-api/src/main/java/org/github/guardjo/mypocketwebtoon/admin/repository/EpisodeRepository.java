package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeEntity;
import org.github.guardjo.mypocketwebtoon.admin.repository.querydsl.EpisodeSearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<EpisodeEntity, Long>, EpisodeSearchRepository {
    /**
     * work_id에 해당하는 episode Entity 의 개수를 반환한다.
     *
     * @param workId work Entity 식별키
     * @return 해당하는 Entity의 총 개수
     */
    long countAllByWork_Id(Long workId);

    /**
     * work_id에 해당하는 episode Entity 목록을 반환한다.
     *
     * @param workId 작품 식별키
     * @return 해당하는 episode Entity 목록
     */
    List<EpisodeEntity> findAllByWork_Id(Long workId);
}
