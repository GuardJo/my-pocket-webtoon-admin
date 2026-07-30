package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.UserInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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

    private AdminInfoEntity testAdmin;

    @BeforeEach
    void setUp() {
        AdminInfoEntity adminInfoEntity = TestDataGenerator.adminInfoEntity("admin", "admin_name");
        adminRoleRepository.save(adminInfoEntity.getRole());
        testAdmin = adminInfoRepository.save(adminInfoEntity);
    }

    @AfterEach
    void tearDown() {
        userInfoRepository.deleteAll();
        adminInfoRepository.deleteAll();
        adminRoleRepository.deleteAll();
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

    private static Stream<Arguments> paginationParams() {
        return Stream.of(
                Arguments.of(0, 10),
                Arguments.of(1, 10),
                Arguments.of(0, 1)
        );
    }
}