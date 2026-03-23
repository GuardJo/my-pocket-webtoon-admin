package org.github.guardjo.mypocketwebtoon.admin.service;

import jakarta.persistence.EntityNotFoundException;
import org.github.guardjo.mypocketwebtoon.admin.config.auth.JwtProvider;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.repository.AdminInfoRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.impl.AdminUserServiceImpl;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {
    private static final AdminInfoEntity TEST_ADMIN = TestDataGenerator.adminInfoEntity("tester", "tester");

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminInfoRepository adminInfoRepository;

    @DisplayName("로그인 성공")
    @Test
    void test_getAccessToken_success() {
        String testPassword = "plain-password";
        String encodePassword = TEST_ADMIN.getPassword();
        String expectedToken = "access-token";

        given(adminInfoRepository.findById(eq(TEST_ADMIN.getId()))).willReturn(Optional.of(TEST_ADMIN));
        given(passwordEncoder.matches(eq(testPassword), eq(encodePassword))).willReturn(true);
        given(jwtProvider.generateAccessToken(eq(TEST_ADMIN))).willReturn(expectedToken);

        String actual = adminUserService.getAccessToken(TEST_ADMIN.getId(), testPassword);

        assertThat(actual).isEqualTo(expectedToken);

        then(adminInfoRepository).should().findById(eq(TEST_ADMIN.getId()));
        then(passwordEncoder).should().matches(eq(testPassword), eq(encodePassword));
        then(jwtProvider).should().generateAccessToken(eq(TEST_ADMIN));
    }

    @DisplayName("로그인 실패 - 아이디 조회 실패")
    @Test
    void test_getAccessToken_fail_notFoundAdminId() {
        String adminId = "missing-admin";
        String testPassword = "test";

        given(adminInfoRepository.findById(eq(adminId))).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getAccessToken(adminId, testPassword))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Not found admin_info");

        then(adminInfoRepository).should().findById(eq(adminId));
        then(passwordEncoder).shouldHaveNoInteractions();
        then(jwtProvider).shouldHaveNoInteractions();
    }

    @DisplayName("로그인 실패 - 비밀번호 검증 실패")
    @Test
    void test_getAccessToken_fail_invalidPassword() {
        String testPassword = "test";
        given(adminInfoRepository.findById(eq(TEST_ADMIN.getId()))).willReturn(Optional.of(TEST_ADMIN));
        given(passwordEncoder.matches(eq(testPassword), eq(TEST_ADMIN.getPassword()))).willReturn(false);

        assertThatCode(() -> adminUserService.getAccessToken(TEST_ADMIN.getId(), testPassword))
                .isInstanceOf(BadCredentialsException.class);

        then(adminInfoRepository).should().findById(eq(TEST_ADMIN.getId()));
        then(passwordEncoder).should().matches(eq(testPassword), eq(TEST_ADMIN.getPassword()));
        then(jwtProvider).shouldHaveNoInteractions();
    }
}
