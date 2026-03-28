package org.github.guardjo.mypocketwebtoon.admin.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.github.guardjo.mypocketwebtoon.admin.model.request.LoginRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;

@Tag(name = "관리자 계정 API", description = "관리자 계정 관련 API 모음")
public interface AdminUserApiDocs {
    @Operation(summary = "로그인", description = "회원 정보를 기반으로 로그인 인증 토큰을 반환한다.")
    BaseResponse<String> login(LoginRequest loginRequest);
}
