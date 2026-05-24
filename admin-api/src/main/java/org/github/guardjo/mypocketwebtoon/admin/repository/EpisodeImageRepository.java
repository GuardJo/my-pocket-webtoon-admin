package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EpisodeImageRepository extends JpaRepository<EpisodeImageEntity, Long> {
    /**
     * 주어진 항목 내 episode 식별키가 포함된 episode_Image를 삭제한다.
     *
     * @param episodeIds episode 식별키 목록
     * @return 삭제된 row 수
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from EpisodeImageEntity e where e.episode.id in :episodeIds")
    long deleteAllByEpisodeIdIn(List<Long> episodeIds);
}
