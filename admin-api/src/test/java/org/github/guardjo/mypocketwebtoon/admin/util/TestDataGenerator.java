package org.github.guardjo.mypocketwebtoon.admin.util;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminRoleEntity;

public class TestDataGenerator {
    private TestDataGenerator() {
        // Constant Class
    }

    public static AdminRoleEntity adminRoleEntity(String id) {
        return AdminRoleEntity.builder()
                .id(id)
                .description("test")
                .activate(true)
                .build();
    }

    public static AdminInfoEntity adminInfoEntity(String id, String name) {
        AdminRoleEntity roleEntity = adminRoleEntity("TEST");

        return AdminInfoEntity.builder()
                .id(id)
                .password("test-password")
                .name(name)
                .activate(true)
                .role(roleEntity)
                .build();
    }
}
