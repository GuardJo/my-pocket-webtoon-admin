package org.github.guardjo.mypocketwebtoon.admin.service;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.UserInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.request.UserCreateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserManagementMetric;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserMetricCountInfo;
import org.github.guardjo.mypocketwebtoon.admin.repository.AdminInfoRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.UserInfoRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.impl.UserServiceImpl;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private final static AdminInfoEntity TEST_ADMIN = TestDataGenerator.adminInfoEntity("tester", "테스터");

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserInfoRepository userInfoRepository;

    @Mock
    private AdminInfoRepository adminInfoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @DisplayName("회원 목록 페이징 조회")
    @Test
    void test_getUserList() {
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
                .comparingOnlyFields("id", "name", "nickname", "activate")
                .isEqualTo(userInfoEntityList);

        then(userInfoRepository).should().findAll(eq(pageRequest));
    }

    @DisplayName("신규 회원 저장")
    @Test
    void test_createUser() {
        UserCreateRequest createRequest = new UserCreateRequest(
                "new",
                "new_name",
                "new_nickname",
                "new_password1!",
                LocalDate.now()
        );

        ArgumentCaptor<UserInfoEntity> userInfoEntityCaptor = ArgumentCaptor.forClass(UserInfoEntity.class);

        given(adminInfoRepository.getReferenceById(eq(TEST_ADMIN.getId()))).willReturn(TEST_ADMIN);
        given(userInfoRepository.findById(eq(createRequest.id()))).willReturn(Optional.empty());
        given(passwordEncoder.encode(eq(createRequest.password()))).willReturn(createRequest.password());
        given(userInfoRepository.save(userInfoEntityCaptor.capture())).willReturn(TestDataGenerator.userInfoEntity(createRequest.id(), TEST_ADMIN));

        assertThatCode(() -> userService.createUser(createRequest, TEST_ADMIN.getId())).doesNotThrowAnyException();

        UserInfoEntity savedUserInfo = userInfoEntityCaptor.getValue();

        assertThat(savedUserInfo.getId()).isEqualTo(createRequest.id());
        assertThat(savedUserInfo.getName()).isEqualTo(createRequest.name());
        assertThat(savedUserInfo.getNickname()).isEqualTo(createRequest.nickname());
        assertThat(savedUserInfo.getPassword()).isEqualTo(createRequest.password());
        assertThat(savedUserInfo.getBirthYmd()).isEqualTo(createRequest.birthYmd());

        then(adminInfoRepository).should().getReferenceById(eq(TEST_ADMIN.getId()));
        then(userInfoRepository).should().findById(eq(createRequest.id()));
        then(passwordEncoder).should().encode(eq(createRequest.password()));
        then(userInfoRepository).should().save(any(UserInfoEntity.class));
    }

    @DisplayName("신규 회원 저장 : 이미 저장된 회원인 경우")
    @Test
    void test_createUser_duplicate_user() {
        UserCreateRequest createRequest = new UserCreateRequest(
                "new",
                "new_name",
                "new_nickname",
                "new_password1!",
                LocalDate.now()
        );

        given(adminInfoRepository.getReferenceById(eq(TEST_ADMIN.getId()))).willReturn(TEST_ADMIN);
        given(userInfoRepository.findById(eq(createRequest.id()))).willReturn(Optional.of(mock(UserInfoEntity.class)));

        assertThatThrownBy(() -> userService.createUser(createRequest, TEST_ADMIN.getId())).isInstanceOf(DuplicateKeyException.class);

        then(adminInfoRepository).should().getReferenceById(eq(TEST_ADMIN.getId()));
        then(userInfoRepository).should().findById(eq(createRequest.id()));
        then(passwordEncoder).shouldHaveNoInteractions();
        then(userInfoRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("회원 관리 메트릭 조회")
    @Test
    void test_getUserManagementMetric() {
        UserMetricCountInfo countInfo = new UserMetricCountInfo(100L, 60L, 40L, 25L, 15L);

        given(userInfoRepository.calculateUserManagementMetric(any(LocalDate.class))).willReturn(countInfo);

        UserManagementMetric actual = userService.getUserManagementMetric();

        assertThat(actual).isEqualTo(new UserManagementMetric(100L, 60L, 40L, 60.0f, 10L));
        then(userInfoRepository).should().calculateUserManagementMetric(any(LocalDate.class));
    }

    @DisplayName("회원 관리 메트릭 조회 : Repository에서 예외가 발생한 경우")
    @Test
    void test_getUserManagementMetric_repository_exception() {
        RuntimeException repositoryException = new RuntimeException("Failed to calculate user management metric");

        given(userInfoRepository.calculateUserManagementMetric(any(LocalDate.class))).willThrow(repositoryException);

        assertThatThrownBy(userService::getUserManagementMetric).isSameAs(repositoryException);
        then(userInfoRepository).should().calculateUserManagementMetric(any(LocalDate.class));
    }
}
