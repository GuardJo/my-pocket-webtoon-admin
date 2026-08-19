package org.github.guardjo.mypocketwebtoon.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.UserInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.request.UserCreateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserManagementMetric;
import org.github.guardjo.mypocketwebtoon.admin.repository.AdminInfoRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.UserInfoRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserInfoRepository userInfoRepository;
    private final AdminInfoRepository adminInfoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public Page<UserInfo> getUserList(Pageable pageable) {
        log.info("Getting user list, pageSize = {}, pageNumber = {}", pageable.getPageSize(), pageable.getPageNumber());

        Page<UserInfoEntity> userInfoEntities = userInfoRepository.findAll(pageable);

        return userInfoEntities.map(UserInfo::of);
    }

    @Transactional
    @Override
    public void createUser(UserCreateRequest createRequest, String adminId) {
        log.info("Creating user, createId= {}, adminId = {}", createRequest.id(), adminId);

        AdminInfoEntity adminInfoEntity = adminInfoRepository.getReferenceById(adminId);
        if (userInfoRepository.findById(createRequest.id()).isPresent()) {
            log.warn("User already exists, createId= {}", createRequest.id());

            throw new DuplicateKeyException("이미 존재하는 아이디 입니다.");
        }

        UserInfoEntity userInfoEntity = UserInfoEntity.builder()
                .id(createRequest.id())
                .name(createRequest.name())
                .nickname(createRequest.nickname())
                .password(passwordEncoder.encode(createRequest.password()))
                .birthYmd(createRequest.birthYmd())
                .adminInfo(adminInfoEntity)
                .build();

        userInfoRepository.save(userInfoEntity);

        log.info("Created user, createId= {}, adminId = {}", userInfoEntity.getId(), userInfoEntity.getAdminInfo().getId());
    }

    @Override
    public UserManagementMetric getUserManagementMetric() {
        // TODO 기능 구현 예정
        return new UserManagementMetric(
                100L,
                50L,
                50L,
                50.0f,
                10L
        );
    }
}
