package org.github.guardjo.mypocketwebtoon.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.repository.AdminInfoRepository;
import org.github.guardjo.mypocketwebtoon.admin.security.JwtProvider;
import org.github.guardjo.mypocketwebtoon.admin.service.AdminUserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {
    private final JwtProvider jwtProvider;
    private final AdminInfoRepository adminInfoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String getAccessToken(String adminId, String password) {
        AdminInfoEntity adminInfo = getAdminInfo(adminId);

        if (!validatePassword(adminInfo.getPassword(), password)) {
            log.error("Invalid password, adminId = {}", adminId);
            throw new BadCredentialsException("Invalid password");
        }

        return jwtProvider.generateAccessToken(adminInfo);
    }

    /*
    아이디를 기준으로 해당하는 AdminInfo Entity 반환
     */
    private AdminInfoEntity getAdminInfo(String adminId) {
        return adminInfoRepository.findById(adminId)
                .orElseThrow(() -> {
                    log.error("Not found admin_info, id = {}", adminId);
                    return new UsernameNotFoundException("Not found adminId");
                });
    }

    /*
    암호 검증
     */
    private boolean validatePassword(String encryptPassword, String plainPassword) {
        return passwordEncoder.matches(plainPassword, encryptPassword);
    }
}
