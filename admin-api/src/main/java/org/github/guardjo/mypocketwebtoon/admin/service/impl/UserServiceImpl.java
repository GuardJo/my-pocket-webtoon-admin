package org.github.guardjo.mypocketwebtoon.admin.service.impl;

import org.github.guardjo.mypocketwebtoon.admin.model.vo.UserInfo;
import org.github.guardjo.mypocketwebtoon.admin.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public Page<UserInfo> getUserList(Pageable pageable) {
        // TODO 기능 구현하기
        List<UserInfo> userList = List.of(
                new UserInfo("tester1", "테스터1", "nickname01", LocalDate.of(2026, 2, 2), true),
                new UserInfo("tester2", "테스터2", "nickname01", LocalDate.of(2026, 2, 1), true)
        );

        return new PageImpl<>(userList, pageable, userList.size());
    }
}
