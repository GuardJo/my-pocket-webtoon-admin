package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<EpisodeEntity, Long> {
    /**
     * work_id에 해당하는 episode Entity 의 개수를 반환한다.
     *
     * @param workId work Entity 식별키
     * @return 해당하는 Entity의 총 개수
     */
    long countAllByWork_Id(Long workId);
}
