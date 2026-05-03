package org.github.guardjo.mypocketwebtoon.admin.model.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "episode_image",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_episode_image_episode_id_sort_order", columnNames = {"episode_id", "sort_order"})
        })
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class EpisodeImageEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "episode_id")
    private EpisodeEntity episode;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, unique = true)
    private String fileUrl;

    @Column(nullable = false)
    private long fileSize;
}
