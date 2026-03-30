package org.github.guardjo.mypocketwebtoon.admin.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "admin_role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString
@EqualsAndHashCode(of = "id", callSuper = false)
public class AdminRoleEntity extends BaseEntity {
    @Id
    @Column(length = 30)
    private String id;

    @Column(length = 100)
    private String description;

    @Setter
    private boolean activate;
}
