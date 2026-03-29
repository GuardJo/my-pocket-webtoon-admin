package org.github.guardjo.mypocketwebtoon.admin.security;

import org.github.guardjo.mypocketwebtoon.admin.model.vo.AdminInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AdminUserPrincipal implements UserDetails {
    private final AdminInfo adminInfo;

    public AdminUserPrincipal(AdminInfo adminInfo) {
        this.adminInfo = adminInfo;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(adminInfo.roleId()));
    }

    @Override
    public String getPassword() {
        return adminInfo.password();
    }

    @Override
    public String getUsername() {
        return adminInfo.id();
    }

    @Override
    public boolean isAccountNonExpired() {
        return adminInfo.activate();
    }

    @Override
    public boolean isAccountNonLocked() {
        return adminInfo.activate();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return adminInfo.activate();
    }

    @Override
    public boolean isEnabled() {
        return adminInfo.activate();
    }
}
