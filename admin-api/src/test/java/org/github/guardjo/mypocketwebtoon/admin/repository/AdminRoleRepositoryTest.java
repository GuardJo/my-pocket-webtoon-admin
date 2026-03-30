package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminRoleEntity;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminRoleRepositoryTest {
    private final AdminRoleEntity TEST_ROLE = TestDataGenerator.adminRoleEntity("TEST");

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @BeforeEach
    void setUp() {
        adminRoleRepository.save(TEST_ROLE);
    }

    @AfterEach
    void tearDown() {
        adminRoleRepository.deleteAll();
    }

    @DisplayName("특정 admin_role Entity 조회")
    @Test
    void test_findById() {
        String roleId = TEST_ROLE.getId();

        AdminRoleEntity adminRoleEntity = adminRoleRepository.findById(roleId)
                .orElseThrow();

        assertThat(adminRoleEntity).isNotNull();
        assertThat(adminRoleEntity).isEqualTo(TEST_ROLE);
    }
}