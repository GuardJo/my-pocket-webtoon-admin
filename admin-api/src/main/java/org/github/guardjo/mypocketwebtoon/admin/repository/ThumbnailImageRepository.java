package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.ThumbnailImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThumbnailImageRepository extends JpaRepository<ThumbnailImageEntity, Long> {
}
