package org.github.guardjo.mypocketwebtoon.admin.model.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "episode",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_episode_work_id_episode_no", columnNames = {"work_id", "episode_no"})
        },
        indexes = {
                @Index(name = "idx_episode_work_id", columnList = "work_id")
        }
)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class EpisodeEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "work_id")
    private WorkEntity work;

    @Column(nullable = false)
    private int episodeNo;

    @OneToOne
    @JoinColumn(name = "thumbnail_id")
    private ThumbnailImageEntity thumbnailImage;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int viewCount;
}
