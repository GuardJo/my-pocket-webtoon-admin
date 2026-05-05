package org.github.guardjo.mypocketwebtoon.admin.service;

import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkService {
    /**
     * 주어진 입력 정보를 기반으로 신규 작품 정보를 저장한다.
     *
     * @param uploadRequest 업로드할 작품 정보
     */
    void uploadWork(WorkUploadRequest uploadRequest);

    /**
     * 주어진 입력 정보를 기반으로 작품 목록을 반환한다.
     *
     * @param pageable 페이징 처리 설정 값
     * @return 페이징 처리된 작품 정보 목록
     */
    Page<WorkSummary> getWorkSummaries(Pageable pageable);

    /**
     * 특정 작품 식별키에 해당하는 작품 정보를 반환한다.
     *
     * @param workId 작품 식별키
     * @return 에피소드 수량을 포함한 작품 정보
     * @throws jakarta.persistence.EntityNotFoundException 해당하는 작품 정보를 찾지 못했을 경우
     */
    WorkInfo getWorkInfo(Long workId);
}
