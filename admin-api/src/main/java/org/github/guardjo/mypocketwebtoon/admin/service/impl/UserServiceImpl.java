package org.github.guardjo.mypocketwebtoon.admin.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.UserInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.request.UserCreateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserDetailInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserManagementMetric;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserMetricCountInfo;
import org.github.guardjo.mypocketwebtoon.admin.repository.AdminInfoRepository;
import org.github.guardjo.mypocketwebtoon.admin.repository.UserInfoRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
        LocalDate currentDate = LocalDate.now();

        log.info("Getting user management metric, currentDate = {}", currentDate);

        UserMetricCountInfo countInfo = userInfoRepository.calculateUserManagementMetric(currentDate);

        return convertMetricInfo(countInfo);
    }

    @Override
    public UserDetailInfo getUserDetail(String userId) {
        log.debug("Getting user detail, userId = {}", userId);

        UserInfoEntity userInfoEntity = userInfoRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        return UserDetailInfo.from(userInfoEntity);
    }

    private UserManagementMetric convertMetricInfo(UserMetricCountInfo countInfo) {
        return new UserManagementMetric(
                countInfo.totalCount(),
                countInfo.activeCount(),
                countInfo.pendingCount(),
                calculateRetentionRate(countInfo.totalCount(), countInfo.activeCount()),
                countInfo.currentMonthCount() - countInfo.lastMonthCount()
        );
    }

    // FIXME 추후 유지율 관련 계산 방식 변경 에정 (단순 활성 비율 ->  근 한달 안에 로그인한 회원 비율)
    private float calculateRetentionRate(long totalCount, long activeCount) {
        if (activeCount == 0) {
            return 0.0f;
        }

        return Math.round((activeCount * 100f / totalCount) * 100f) / 100f;
    }
}
