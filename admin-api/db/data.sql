--- 기본 관리자 권한
insert into admin_role
values ('MASTER', '마스터 관리자', true, current_timestamp, current_timestamp);
insert into admin_role
values ('ADMIN', '관리자', true, current_timestamp, current_timestamp);
insert into admin_role
values ('BASIC', '기본', true, current_timestamp, current_timestamp);

--- 기본 관리자 계정
insert into admin_info
values ('admin', '관리자', '{noop}password1!', true, 'ADMIN', current_timestamp, current_timestamp);

--- 테스트 사용자
insert into user_info
values ('tester001', '테스터', '킹왕짱테스터', '{noop}test123!', true, '1996-02-20', 'admin', current_timestamp,
        current_timestamp);
insert into user_info
values ('tester002', '테스터2', '킹왕짱테스터2', '{noop}test123!', true, '1996-02-20', 'admin', current_timestamp,
        current_timestamp);
insert into user_info
values ('tester003', '테스터3', '킹왕짱테스터3', '{noop}test123!', true, '1996-02-20', 'admin', current_timestamp,
        current_timestamp);
