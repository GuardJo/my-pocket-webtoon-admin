package org.github.guardjo.mypocketwebtoon.admin.security;

import jakarta.servlet.FilterChain;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.AdminInfo;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    private final static AdminInfo TEST_ADMIN_INFO = AdminInfo.of(TestDataGenerator.adminInfoEntity("tester", "테스터"));

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("Bearer 토큰이 존재하면 인증 정보를 SecurityContext에 저장한다")
    @Test
    void test_doFilterInternal_success() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "access-token";
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        given(jwtProvider.getUserKey(eq(token))).willReturn(TEST_ADMIN_INFO.id());
        given(userDetailsService.loadUserByUsername(eq(TEST_ADMIN_INFO.id()))).willReturn(new AdminUserPrincipal(TEST_ADMIN_INFO));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isInstanceOf(AdminUserPrincipal.class);
        assertThat(authentication.getName()).isEqualTo(TEST_ADMIN_INFO.id());
        assertThat(authentication.getCredentials()).isEqualTo(TEST_ADMIN_INFO.password());
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_" + TEST_ADMIN_INFO.roleId());

        then(jwtProvider).should().getUserKey(eq(token));
        then(userDetailsService).should().loadUserByUsername(eq(TEST_ADMIN_INFO.id()));
        then(filterChain).should().doFilter(eq(request), eq(response));
    }

    @DisplayName("Authorization 헤더가 없으면 인증 없이 다음 필터로 진행한다")
    @Test
    void test_doFilterInternal_without_authorization_header() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        then(jwtProvider).shouldHaveNoInteractions();
        then(userDetailsService).shouldHaveNoInteractions();
        then(filterChain).should().doFilter(eq(request), eq(response));
    }

    @DisplayName("Bearer 형식이 아닌 Authorization 헤더는 무시한다")
    @Test
    void test_doFilterInternal_ignore_invalid_authorization_header() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic invalid-token");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        then(jwtProvider).shouldHaveNoInteractions();
        then(userDetailsService).shouldHaveNoInteractions();
        then(filterChain).should().doFilter(eq(request), eq(response));
    }

    @DisplayName("토큰 인증 중 예외가 발생하면 다음 필터로 넘기지 않고 예외를 전파한다")
    @Test
    void test_doFilterInternal_fail_when_getAdminInfo_throws_exception() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "access-token";
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        given(jwtProvider.getUserKey(eq(token))).willReturn(TEST_ADMIN_INFO.id());
        willThrow(UsernameNotFoundException.class).given(userDetailsService).loadUserByUsername(eq(TEST_ADMIN_INFO.id()));

        assertThatThrownBy(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain))
                .isInstanceOf(UsernameNotFoundException.class);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        then(jwtProvider).should().getUserKey(eq(token));
        then(userDetailsService).should().loadUserByUsername(eq(TEST_ADMIN_INFO.id()));
        then(filterChain).shouldHaveNoInteractions();
    }
}
