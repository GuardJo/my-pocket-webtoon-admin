package org.github.guardjo.mypocketwebtoon.admin.model.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "work",
        indexes = {
                @Index(name = "idx_work_modified_at", columnList = "modified_at"),
                @Index(name = "idx_work_visibility", columnList = "visibility")
        })
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class WorkEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false, unique = true)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 10, nullable = false)
    private String serialState = "COMPLETED";

    @OneToOne
    @JoinColumn(name = "thumbnail_id")
    private ThumbnailImageEntity thumbnailImage;

    @Column(nullable = false)
    private boolean visibility;
}
