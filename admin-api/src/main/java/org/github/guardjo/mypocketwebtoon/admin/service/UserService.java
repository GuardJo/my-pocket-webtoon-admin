package org.github.guardjo.mypocketwebtoon.admin.service;

import org.github.guardjo.mypocketwebtoon.admin.model.request.UserCreateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserDetailInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserManagementMetric;
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
     * @throws org.springframework.dao.DataIntegrityViolationException 회원닉네임이 이미 존재하는 경우
     * @throws org.springframework.dao.DuplicateKeyException           회원 아이디가 이미 존재하는 경우
     */
    void createUser(UserCreateRequest createRequest, String adminId);

    /**
     * 현재 일자 기준 관리중인 회원 관련 매트릭 정보를 계산하여 반환한다.
     *
     * @return 계산된 회원 매트릭 정보
     */
    UserManagementMetric getUserManagementMetric();

    /**
     * 주어진 회원 식별키에 해당하는 회원에 대한 상세 정보를 반환한다.
     *
     * @param userId 회원 식별키
     * @return 회원 상세 정보
     * @throws jakarta.persistence.EntityNotFoundException 회원 정보를 찾을 수 없는 경우
     */
    UserDetailInfo getUserDetail(String userId);
}
