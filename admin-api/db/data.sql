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