package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.api.docs.AdminUserApiDocs;
import org.github.guardjo.mypocketwebtoon.admin.model.request.LoginRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.service.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController implements AdminUserApiDocs {
    private final AdminUserService adminUserService;

    @PostMapping("/login")
    @Override
    public BaseResponse<String> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("POST : /api/v1/auth/login, adminId = {}", loginRequest.id());

        String token = adminUserService.getAccessToken(loginRequest.id(), loginRequest.password());

        return BaseResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .statusCode(HttpStatus.OK.name())
                .data(token)
                .build();
    }
}
