package org.github.guardjo.mypocketwebtoon.admin.service;

import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUpdateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.EpisodeInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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
    WorkInfo getWorkInfo(long workId);

    /**
     * 주어진 작품 식별키에 해당하는 에피소드 정보 목록을 반환한다.
     *
     * @param workId   작품 식별키
     * @param pageable 페이젠이션 설정
     * @return 페이징 처리된 작품 내 에피소드 정보 목록
     */
    Page<EpisodeInfo> getEpisodeInfosByWork(long workId, Pageable pageable);

    /**
     * 주어진 작품 식별키에 해당하는 관련 데이터들을 제거한다.
     *
     * @param workId 작품 식별키
     */
    void clearWorkData(long workId);

    /**
     * 식별키에 해당하는 작품 정보를 주어진 데이터 기반으로 수정한다.
     *
     * @param workId        작품 식별키
     * @param updateRequest 업데이트할 데이터
     */
    void updateWork(long workId, WorkUpdateRequest updateRequest);

    /**
     * 식별키에 해당하는 작품에 대한 작품 썸네일을 수정한다.
     *
     * @param workId             작품 식별키
     * @param thumbnailImageFile 작품 썸네일 파일
     */
    void updateWorkThumbnailImage(long workId, MultipartFile thumbnailImageFile);
}
