package org.github.guardjo.mypocketwebtoon.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.UserInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.repository.UserInfoRepository;
import org.github.guardjo.mypocketwebtoon.admin.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserInfoRepository userInfoRepository;

    @Override
    public Page<UserInfo> getUserList(Pageable pageable) {
        log.info("Getting user list, pageSize = {}, pageNumber = {}", pageable.getPageSize(), pageable.getPageNumber());

        Page<UserInfoEntity> userInfoEntities = userInfoRepository.findAll(pageable);

        return userInfoEntities.map(UserInfo::of);
    }
}
