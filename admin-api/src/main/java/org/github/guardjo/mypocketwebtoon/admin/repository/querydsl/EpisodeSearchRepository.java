package org.github.guardjo.mypocketwebtoon.admin.repository.querydsl;

import org.github.guardjo.mypocketwebtoon.admin.model.vo.EpisodeInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EpisodeSearchRepository {
    /**
     * 특정 작품 내 에피소드별 정보 목록 조회
     *
     * @param workId   작품 식별키
     * @param pageable 페이징 처리
     * @return 페이징 처리된 에피소드 정보 목록
     */
    Page<EpisodeInfo> findAllByWorkId(Long workId, Pageable pageable);
}
