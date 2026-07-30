package org.github.guardjo.mypocketwebtoon.admin.service;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.UserInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.repository.UserInfoRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.impl.UserServiceImpl;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private final static AdminInfoEntity TEST_ADMIN = TestDataGenerator.adminInfoEntity("tester", "테스터");

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserInfoRepository userInfoRepository;

    @DisplayName("회원 목록 페이징 조회")
    @Test
    void testa_getUserList() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        List<UserInfoEntity> userInfoEntityList = List.of(
                TestDataGenerator.userInfoEntity("test1", TEST_ADMIN),
                TestDataGenerator.userInfoEntity("test2", TEST_ADMIN),
                TestDataGenerator.userInfoEntity("test3", TEST_ADMIN)
        );

        given(userInfoRepository.findAll(eq(pageRequest))).willReturn(new PageImpl<>(userInfoEntityList, pageRequest, userInfoEntityList.size()));

        Page<UserInfo> actual = userService.getUserList(pageRequest);
        assertThat(actual).isNotNull();
        assertThat(actual.getTotalElements()).isEqualTo(userInfoEntityList.size());
        assertThat(actual.getContent()).usingRecursiveComparison()
                .comparingOnlyFields("id", "username", "nickname", "activate")
                .isEqualTo(userInfoEntityList);

        then(userInfoRepository).should().findAll(eq(pageRequest));
    }
}