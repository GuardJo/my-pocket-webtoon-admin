--- 관리자 권한
create table admin_role
(
    id          varchar(30) primary key,
    description varchar(100) not null,
    activate    bool         not null default false,
    created_at  timestamp    not null default current_timestamp,
    modified_at timestamp    not null default current_timestamp
);

comment on table admin_role is '관리자 권한 테이블';
comment on column admin_role.id is '관리자 권한 식별키';
comment on column admin_role.description is '권한 설명';
comment on column admin_role.activate is '활성 여부';
comment on column admin_role.created_at is '생성일시';
comment on column admin_role.modified_at is '수정일시';
