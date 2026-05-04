package org.github.guardjo.mypocketwebtoon.admin.repository.querydsl;

import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkSearchRepository {
    /**
     * 입력받은 페이징 값을 기준으로 페이지네이션된 작품 정보 목록을 반환한다.
     *
     * @param pageable 페이지네이션 설정 값
     * @return 페이징 처리된 작품 정보 목록
     */
    Page<WorkSummary> findAllWithPagination(Pageable pageable);
}
