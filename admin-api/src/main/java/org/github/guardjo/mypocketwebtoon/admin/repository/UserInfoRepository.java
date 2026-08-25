package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.UserInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.repository.querydsl.UserMetricRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String>, UserMetricRepository {
}
