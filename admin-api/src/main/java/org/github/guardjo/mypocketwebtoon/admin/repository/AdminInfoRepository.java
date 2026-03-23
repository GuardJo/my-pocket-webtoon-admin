package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminInfoRepository extends JpaRepository<AdminInfoEntity, String> {
}
