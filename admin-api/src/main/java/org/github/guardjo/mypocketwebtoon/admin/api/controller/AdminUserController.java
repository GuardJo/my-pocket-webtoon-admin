package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.api.docs.AdminUserApiDocs;
import org.github.guardjo.mypocketwebtoon.admin.model.request.LoginRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.AdminProfileInfo;
import org.github.guardjo.mypocketwebtoon.admin.security.AdminUserPrincipal;
import org.github.guardjo.mypocketwebtoon.admin.service.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

        return BaseResponse.of(HttpStatus.OK, token);
    }

    @GetMapping("/me")
    @Override
    public BaseResponse<AdminProfileInfo> getMyProfileInfo(@AuthenticationPrincipal AdminUserPrincipal principal) {
        log.info("GET : /api/v1/auth/me, userId = {}", principal.getUsername());

        AdminProfileInfo profileInfo = AdminProfileInfo.of(principal.getAdminInfo());

        return BaseResponse.of(HttpStatus.OK, profileInfo);
    }

    @PostMapping("/upload-token")
    @Override
    public BaseResponse<String> getFileUploadToken(@AuthenticationPrincipal AdminUserPrincipal principal) {
        log.info("POST : /api/v1/auth/upload-token");

        String token = adminUserService.getTemporarilyAccessToken(principal.getAdminInfo());

        return BaseResponse.of(HttpStatus.OK, token);
    }
}
