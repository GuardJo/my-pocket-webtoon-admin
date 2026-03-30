package org.github.guardjo.mypocketwebtoon.admin.model.vo;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;

public record AdminInfo(
        String id,
        String password,
        String name,
        boolean activate,
        String roleId
) {
    public static AdminInfo of(AdminInfoEntity entity) {
        return new AdminInfo(
                entity.getId(),
                entity.getPassword(),
                entity.getName(),
                entity.isActivate(),
                entity.getRole().getId()
        );
    }
}
