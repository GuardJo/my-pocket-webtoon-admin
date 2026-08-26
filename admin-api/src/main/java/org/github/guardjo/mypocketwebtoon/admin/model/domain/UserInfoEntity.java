package org.github.guardjo.mypocketwebtoon.admin.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Table(name = "user_info", indexes = {
        @Index(name = "idx_user_info_name", columnList = "name"),
        @Index(name = "idx_user_info_nickname", columnList = "nickname"),
        @Index(name = "idx_user_info_register_admin_id", columnList = "register_admin_id")
})
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class UserInfoEntity extends BaseEntity {
    @Id
    @Column(length = 20, nullable = false, unique = true)
    private String id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String nickname;

    @Column(length = 300, nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean activate;

    @Column(name = "birth_ymd")
    private LocalDate birthYmd;

    @ManyToOne
    @JoinColumn(name = "register_admin_id")
    private AdminInfoEntity adminInfo;
}
