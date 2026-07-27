package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.api.docs.UserApiDocs;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController implements UserApiDocs {
    private final UserService userService;

    @GetMapping
    @Override
    public BaseResponse<PagedModel<UserInfo>> getUsers(@PageableDefault(sort = "signupDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET : /api/v1/users, pageNumber = {}, pageSize = {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<UserInfo> userInfoPage = userService.getUserList(pageable);

        return BaseResponse.of(HttpStatus.OK, new PagedModel<>(userInfoPage));
    }
}
