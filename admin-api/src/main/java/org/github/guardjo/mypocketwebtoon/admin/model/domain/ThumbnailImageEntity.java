package org.github.guardjo.mypocketwebtoon.admin.model.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "thumbnail_image")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class ThumbnailImageEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false, unique = true)
    private String fileUrl;

    private int fileSize;
}
