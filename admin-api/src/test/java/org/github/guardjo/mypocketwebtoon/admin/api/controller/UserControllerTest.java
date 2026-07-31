package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.guardjo.mypocketwebtoon.admin.config.StaticResourceConfig;
import org.github.guardjo.mypocketwebtoon.admin.model.request.UserCreateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.AdminInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.security.AdminUserPrincipal;
import org.github.guardjo.mypocketwebtoon.admin.service.UserService;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = StaticResourceConfig.class
        )
)
class UserControllerTest extends AbstractPageableControllerTest {
    private final static AdminUserPrincipal TEST_USER = new AdminUserPrincipal(AdminInfo.of(TestDataGenerator.adminInfoEntity("test", "tester")));

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @DisplayName("GET : /api/v1/users")
    @Test
    void test_getUsers() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));

        List<UserInfo> userInfoList = List.of(
                TestDataGenerator.userInfo("test1", "테스터1"),
                TestDataGenerator.userInfo("test2", "테스터2")
        );

        Page<UserInfo> expected = new PageImpl<>(userInfoList);

        given(userService.getUserList(eq(pageRequest))).willReturn(expected);

        String response = mockMvc.perform(get("/api/v1/users")
                        .param("page", String.valueOf(pageRequest.getPageNumber()))
                        .param("size", String.valueOf(pageRequest.getPageSize()))
                        .with(user(TEST_USER)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType pageResponseType = objectMapper.getTypeFactory().constructParametricType(MockPageResponse.class, UserInfo.class);
        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, pageResponseType);

        BaseResponse<MockPageResponse<UserInfo>> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK.name());
        assertThat(actual.getData().page().totalElements()).isEqualTo(2L);
        assertThat(actual.getData().content()).isEqualTo(userInfoList);

        then(userService).should().getUserList(eq(pageRequest));
    }

    @DisplayName("GET : /api/v1/users : 조회 결과 데이터가 없을 경우")
    @Test
    void test_getUsers_empty() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));

        Page<UserInfo> expected = new PageImpl<>(List.of());

        given(userService.getUserList(eq(pageRequest))).willReturn(expected);

        String response = mockMvc.perform(get("/api/v1/users")
                        .param("page", String.valueOf(pageRequest.getPageNumber()))
                        .param("size", String.valueOf(pageRequest.getPageSize()))
                        .with(user(TEST_USER)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType pageResponseType = objectMapper.getTypeFactory().constructParametricType(MockPageResponse.class, UserInfo.class);
        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, pageResponseType);

        BaseResponse<MockPageResponse<UserInfo>> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK.name());
        assertThat(actual.getData().page().totalElements()).isZero();
        assertThat(actual.getData().content()).isEmpty();

        then(userService).should().getUserList(eq(pageRequest));
    }

    @DisplayName("POST : /api/v1/users")
    @Test
    void test_createUser() throws Exception {
        UserCreateRequest createRequest = new UserCreateRequest(
                "test",
                "테스터",
                "tester01",
                "password1!",
                null
        );

        String requestContent = objectMapper.writeValueAsString(createRequest);

        String response = mockMvc.perform(post("/api/v1/users")
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(TEST_USER))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK.name());
        assertThat(actual.getData()).isEqualTo("Successes");

        then(userService).should().createUser(eq(createRequest), eq(TEST_USER.getUsername()));
    }

    @DisplayName("POST : /api/v1/users : 요청 정보가 올바르지 않을 경우")
    @Test
    void test_createUser_invalid_request() throws Exception {
        UserCreateRequest createRequest = new UserCreateRequest(
                "",
                "테스터",
                "tester01",
                "password",
                null
        );

        String requestContent = objectMapper.writeValueAsString(createRequest);

        String response = mockMvc.perform(post("/api/v1/users")
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(TEST_USER))
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

        then(userService).should(never()).createUser(any(), any());
    }

    @DisplayName("POST : /api/v1/users : 관리자 인증 정보가 없을 경우")
    @Test
    void test_createUser_unauthorized() throws Exception {
        UserCreateRequest createRequest = new UserCreateRequest(
                "test",
                "테스터",
                "tester01",
                "password1!",
                null
        );

        String requestContent = objectMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/api/v1/users")
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        then(userService).should(never()).createUser(any(), any());
    }
}
