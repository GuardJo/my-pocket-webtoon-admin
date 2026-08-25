package org.github.guardjo.mypocketwebtoon.admin.repository;

import jakarta.persistence.EntityManager;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.UserInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserMetricCountInfo;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserInfoRepositoryTest {
    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private AdminInfoRepository adminInfoRepository;

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @Autowired
    private EntityManager entityManager;

    private AdminInfoEntity testAdmin;

    @BeforeEach
    void setUp() {
        AdminInfoEntity adminInfoEntity = TestDataGenerator.adminInfoEntity("admin", "admin_name");
        adminRoleRepository.save(adminInfoEntity.getRole());
        testAdmin = adminInfoRepository.save(adminInfoEntity);
    }

    @DisplayName("저장된 UserInfoEntity 페이지 조회")
    @ParameterizedTest
    @MethodSource("paginationParams")
    void test_findAll_with_pagination(int pageNumber, int pageSize) {
        List<UserInfoEntity> userInfoEntityList = List.of(
                TestDataGenerator.userInfoEntity("test1", testAdmin),
                TestDataGenerator.userInfoEntity("test2", testAdmin),
                TestDataGenerator.userInfoEntity("test3", testAdmin)
        );

        userInfoRepository.saveAll(userInfoEntityList);

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by("id"));

        Page<UserInfoEntity> expected = userInfoRepository.findAll(pageRequest);

        assertThat(expected).isNotNull();
        assertThat(expected.getTotalElements()).isEqualTo(userInfoEntityList.size());
        assertThat(expected.getContent()).usingRecursiveComparison()
                .ignoringFields("createdAt", "modifiedAt")
                .isEqualTo(userInfoEntityList.subList(Math.min(pageNumber * pageSize, userInfoEntityList.size()), Math.min((pageNumber + 1) * pageSize, userInfoEntityList.size())));
    }

    @DisplayName("신규 UserInfoEntity 저장")
    @Test
    void test_save() {
        UserInfoEntity expected = TestDataGenerator.userInfoEntity("test", testAdmin);

        userInfoRepository.save(expected);

        UserInfoEntity actual = userInfoRepository.findById(expected.getId())
                .orElseThrow();

        assertThat(actual).usingRecursiveComparison()
                .ignoringFields("createdAt", "modifiedAt")
                .isEqualTo(expected);
    }

    @DisplayName("신규 UserInfoEntity 저장 : 중복 키값 저장 실패")
    @Test
    void test_save_duplicateKey() {
        UserInfoEntity expected = TestDataGenerator.userInfoEntity("test", testAdmin);

        assertThatCode(() -> userInfoRepository.saveAndFlush(expected)).doesNotThrowAnyException();
        assertThatThrownBy(() -> userInfoRepository.saveAndFlush(TestDataGenerator.userInfoEntity("test2", expected.getNickname(), testAdmin)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("기준 월의 회원 관리 지표 계산 및 조회")
    @Test
    void calculateUserManagementMetric() {
        LocalDate currentMonthStart = LocalDate.of(2026, 8, 1);
        List<UserInfoEntity> users = List.of(
                TestDataGenerator.userInfoEntity("before-period", true, testAdmin),
                TestDataGenerator.userInfoEntity("previous-active", true, testAdmin),
                TestDataGenerator.userInfoEntity("previous-pending", false, testAdmin),
                TestDataGenerator.userInfoEntity("current-pending", false, testAdmin)
        );
        userInfoRepository.saveAllAndFlush(users);

        updateCreatedAt("before-period", LocalDateTime.of(2026, 6, 30, 23, 59, 59));
        updateCreatedAt("previous-active", LocalDateTime.of(2026, 7, 1, 0, 0));
        updateCreatedAt("previous-pending", LocalDateTime.of(2026, 7, 31, 23, 59, 59));
        updateCreatedAt("current-pending", LocalDateTime.of(2026, 8, 1, 0, 0));
        entityManager.clear();

        UserMetricCountInfo actual = userInfoRepository.calculateUserManagementMetric(currentMonthStart);

        assertThat(actual).isEqualTo(new UserMetricCountInfo(
                4L,
                2L,
                2L,
                1L,
                2L
        ));
    }

    private void updateCreatedAt(String id, LocalDateTime createdAt) {
        entityManager.createNativeQuery("""
                        update user_info
                        set created_at = :createdAt
                        where id = :id
                        """)
                .setParameter("createdAt", createdAt)
                .setParameter("id", id)
                .executeUpdate();
    }

    private static Stream<Arguments> paginationParams() {
        return Stream.of(
                Arguments.of(0, 10),
                Arguments.of(1, 10),
                Arguments.of(0, 1)
        );
    }
}
