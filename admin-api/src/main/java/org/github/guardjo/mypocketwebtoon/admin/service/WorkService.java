package org.github.guardjo.mypocketwebtoon.admin.service;

import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;

public interface WorkService {
    /**
     * 주어진 입력 정보를 기반으로 신규 작품 정보를 저장한다.
     *
     * @param uploadRequest 업로드할 작품 정보
     */
    void uploadWork(WorkUploadRequest uploadRequest);
}
