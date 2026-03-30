package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminInfoRepositoryTest {
    private final static AdminInfoEntity TEST_ADMIN = TestDataGenerator.adminInfoEntity("tester", "테스터");

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @Autowired
    private AdminInfoRepository adminInfoRepository;

    @BeforeEach
    void setUp() {
        adminRoleRepository.save(TEST_ADMIN.getRole());
        adminInfoRepository.save(TEST_ADMIN);
    }

    @AfterEach
    void tearDown() {
        adminInfoRepository.deleteAll();
        adminRoleRepository.deleteAll();
    }

    @DisplayName("특정 아이디의 AdminInfo Entity 조회")
    @Test
    void test_findById() {
        String adminId = TEST_ADMIN.getId();

        AdminInfoEntity actual = adminInfoRepository.findById(adminId)
                .orElseThrow();

        assertThat(actual).usingRecursiveComparison()
                .ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(TEST_ADMIN);
    }
}