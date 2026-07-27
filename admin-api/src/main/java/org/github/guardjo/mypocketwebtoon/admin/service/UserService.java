package org.github.guardjo.mypocketwebtoon.admin.service;

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
}
