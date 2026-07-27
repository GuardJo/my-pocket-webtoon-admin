package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.guardjo.mypocketwebtoon.admin.config.StaticResourceConfig;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("signupDate")));

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
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("signupDate")));

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
}