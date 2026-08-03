package org.github.guardjo.mypocketwebtoon.admin.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.github.guardjo.mypocketwebtoon.admin.model.request.UserCreateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.security.AdminUserPrincipal;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

@Tag(name = "회원 계정 API", description = "일반 회원 계정 관리 API 모음")
public interface UserApiDocs {
    @Operation(summary = "회원 목록 조회", description = "현재 등록된 회원 목록을 반환한다.")
    BaseResponse<PagedModel<UserInfo>> getUsers(@ParameterObject Pageable pageable);

    @Operation(summary = "회원 등록", description = "신규 회원 정보를 등록한다.")
    BaseResponse<String> createUser(@Parameter(hidden = true) AdminUserPrincipal principal, UserCreateRequest userCreateRequest);
}
