package org.github.guardjo.mypocketwebtoon.admin.model.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "admin_info", indexes = {
        @Index(name = "idx_admin_info_name", columnList = "name")
})
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class AdminInfoEntity extends BaseEntity {
    @Id
    private String id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 300, nullable = false)
    private String password;

    @Setter
    private boolean activate;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private AdminRoleEntity role;
}
