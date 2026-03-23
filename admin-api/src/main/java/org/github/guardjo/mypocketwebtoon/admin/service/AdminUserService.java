package org.github.guardjo.mypocketwebtoon.admin.service;

public interface AdminUserService {
    /**
     * 주어진 계정 아이디와 비밀번호에 매핑되는 회원을 기반으로 인증토큰을 반환한다.
     *
     * @param adminId  관리자 계정 아이디
     * @param password 관리자 계정 비밀번호 (평문)
     * @return JWT 인증 토큰
     */
    String getAccessToken(String adminId, String password);
}
