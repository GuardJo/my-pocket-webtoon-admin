package org.github.guardjo.mypocketwebtoon.admin.service;

import org.github.guardjo.mypocketwebtoon.admin.model.request.UserCreateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    /**
     * 주어진 페이지네이션 기반으로 회원 목록을 반환한다.
     *
     * @param pageable 페이지네이션 옵션
     * @return 페이징 처리된 회원 목록
     */
    Page<UserInfo> getUserList(Pageable pageable);

    /**
     * 주어진 회원 요청 정보를 기반으로 신규 회원을 등록한다.
     *
     * @param createRequest 회원 등록 정보
     * @param adminId       등록 요청 관리자 계정 아이디
     */
    void createUser(UserCreateRequest createRequest, String adminId);
}
