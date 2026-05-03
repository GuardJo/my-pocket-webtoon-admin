package org.github.guardjo.mypocketwebtoon.admin.repository;

import org.github.guardjo.mypocketwebtoon.admin.model.domain.EpisodeImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeImageRepository extends JpaRepository<EpisodeImageEntity, Long> {
}
