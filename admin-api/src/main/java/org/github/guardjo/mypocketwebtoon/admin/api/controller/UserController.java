package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.api.docs.UserApiDocs;
import org.github.guardjo.mypocketwebtoon.admin.model.request.UserCreateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserDetailInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserManagementMetric;
import org.github.guardjo.mypocketwebtoon.admin.security.AdminUserPrincipal;
import org.github.guardjo.mypocketwebtoon.admin.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController implements UserApiDocs {
    private final UserService userService;

    @GetMapping
    @Override
    public BaseResponse<PagedModel<UserInfo>> getUsers(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET : /api/v1/users, pageNumber = {}, pageSize = {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<UserInfo> userInfoPage = userService.getUserList(pageable);

        return BaseResponse.of(HttpStatus.OK, new PagedModel<>(userInfoPage));
    }

    @PostMapping
    @Override
    public BaseResponse<String> createUser(@AuthenticationPrincipal AdminUserPrincipal principal, @RequestBody UserCreateRequest userCreateRequest) {
        log.info("POST: /api/v1/users, adminId = {}, userId = {}", principal.getUsername(), userCreateRequest.id());

        userService.createUser(userCreateRequest, principal.getUsername());

        return BaseResponse.defaultSuccessResponse();
    }

    @GetMapping("/metric")
    @Override
    public BaseResponse<UserManagementMetric> getUserManagementMetric() {
        log.info("GET: /api/v1/users/metric");

        UserManagementMetric metric = userService.getUserManagementMetric();

        return BaseResponse.of(HttpStatus.OK, metric);
    }

    @GetMapping("/{userId}")
    @Override
    public BaseResponse<UserDetailInfo> getUserDetail(@PathVariable String userId) {
        log.info("GET: /api/v1/users/{}", userId);

        UserDetailInfo userDetail = userService.getUserDetail(userId);

        return BaseResponse.of(HttpStatus.OK, userDetail);
    }
}
