package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.guardjo.mypocketwebtoon.admin.config.TestSecurityConfig;
import org.github.guardjo.mypocketwebtoon.admin.model.request.LoginRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.service.AdminUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserController.class)
@Import(value = TestSecurityConfig.class)
class AdminUserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @DisplayName("POST: /api/v1/auth/login")
    @Test
    void test_login() throws Exception {
        LoginRequest loginRequest = new LoginRequest("tester", "test_password");
        String requestContent = objectMapper.writeValueAsString(loginRequest);
        String expectedToken = "test-token";

        given(adminUserService.getAccessToken(eq(loginRequest.id()), eq(loginRequest.password()))).willReturn(expectedToken);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK.name());
        assertThat(actual.getData()).isEqualTo(expectedToken);

        then(adminUserService).should().getAccessToken(eq(loginRequest.id()), eq(loginRequest.password()));
    }

    @DisplayName("POST: /api/v1/auth/login - 로그인 실패(계정 아이디 불일치)")
    @Test
    void test_login_fail_usernameNotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest("unknown", "test_password");
        String requestContent = objectMapper.writeValueAsString(loginRequest);

        given(adminUserService.getAccessToken(eq(loginRequest.id()), eq(loginRequest.password())))
                .willThrow(new UsernameNotFoundException("Not found admin_info"));

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.name());

        then(adminUserService).should().getAccessToken(eq(loginRequest.id()), eq(loginRequest.password()));
    }

    @DisplayName("POST: /api/v1/auth/login - 로그인 실패(비밀번호 불일치)")
    @Test
    void test_login_fail_invalidPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("tester", "invalid_password");
        String requestContent = objectMapper.writeValueAsString(loginRequest);

        given(adminUserService.getAccessToken(eq(loginRequest.id()), eq(loginRequest.password())))
                .willThrow(new BadCredentialsException("Invalid password"));

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.name());

        then(adminUserService).should().getAccessToken(eq(loginRequest.id()), eq(loginRequest.password()));
    }

    @DisplayName("POST: /api/v1/auth/login - 요청 데이터 검증 실패(빈 값)")
    @Test
    void test_login_fail_invalidRequest_blankValue() throws Exception {
        LoginRequest loginRequest = new LoginRequest("", "");
        String requestContent = objectMapper.writeValueAsString(loginRequest);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(actual.getData()).isEqualTo("요청 값이 올바르지 않습니다.");

        then(adminUserService).should(never()).getAccessToken(eq(loginRequest.id()), eq(loginRequest.password()));
    }

    @DisplayName("POST: /api/v1/auth/login - 요청 데이터 검증 실패(아이디 길이 초과)")
    @Test
    void test_login_fail_invalidRequest_invalidSize() throws Exception {
        LoginRequest loginRequest = new LoginRequest("tester-id-over-twenty", "test_password");
        String requestContent = objectMapper.writeValueAsString(loginRequest);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(actual.getData()).isEqualTo("요청 값이 올바르지 않습니다.");

        then(adminUserService).should(never()).getAccessToken(eq(loginRequest.id()), eq(loginRequest.password()));
    }
}
