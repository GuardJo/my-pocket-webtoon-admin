package org.github.guardjo.mypocketwebtoon.admin.model.domain;

import jakarta.persistence.*;
import lombok.*;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUpdateRequest;

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
    @Builder.Default
    private String serialState = "COMPLETED";

    @OneToOne
    @JoinColumn(name = "thumbnail_id")
    @Setter
    private ThumbnailImageEntity thumbnailImage;

    @Column(nullable = false)
    private boolean visibility;

    /**
     * 주어진 데이터를 기반으로 기존 데이터를 갱신한다.
     *
     * @param updateRequest 갱신할 작품 정보 데이터
     */
    public void update(WorkUpdateRequest updateRequest) {
        this.title = updateRequest.title();
        this.description = updateRequest.description();
        this.visibility = updateRequest.visibility();
        this.serialState = updateRequest.serialState();
    }
}
