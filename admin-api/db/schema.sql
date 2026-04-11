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

--- 관리자 계정
create table admin_info
(
    id          varchar(20) primary key,
    name        varchar(50)  not null,
    password    varchar(300) not null,
    activate    bool         not null default false,
    role_id     varchar(30)  not null references admin_role,
    created_at  timestamp    not null default current_timestamp,
    modified_at timestamp    not null default current_timestamp
);

comment on table admin_info is '관리자 계정 테이블';
comment on column admin_info.id is '관리자 계정 식별키';
comment on column admin_info.name is '성명';
comment on column admin_info.password is '비밀번호';
comment on column admin_info.activate is '활성 여부';
comment on column admin_info.role_id is '권한 식별키';
comment on column admin_info.created_at is '생성일시';
comment on column admin_info.modified_at is '수정일시';

create index idx_admin_info_name on admin_info (name);

--- 작품 썸네일 이미지
create sequence seq_thumbnail_image start with 1 increment by 1 cache 50;

create table thumbnail_image
(
    id          bigint primary key   default nextval('seq_thumbnail_image'),
    file_url    text unique not null,
    file_size   integer     not null default 0,
    created_at  timestamp   not null default current_timestamp,
    modified_at timestamp   not null default current_timestamp
);
comment on table thumbnail_image is '작품 썸네일 이미지';
comment on column thumbnail_image.id is '작픔 썸네일 식별키';
comment on column thumbnail_image.file_url is '이미지 파일 url';
comment on column thumbnail_image.file_size is '이미지 파일 크기';
comment on column thumbnail_image.created_at is '생성일시';
comment on column thumbnail_image.modified_at is '수정일시';

--- 작품
create sequence seq_work start with 1 increment by 1 cache 50;

create table work
(
    id           bigint primary key           default nextval('seq_work'),
    title        varchar(100) unique not null,
    description  varchar(500),
    serial_state varchar(10)         not null default 'COMPLETED',
    thumbnail_id bigint references thumbnail_image,
    visibility   bool                not null default false,
    created_at   timestamp           not null default current_timestamp,
    modified_at  timestamp           not null default current_timestamp
);

create index idx_work_visibility on work (visibility);
create index idx_work_modified_at on work (modified_at);

comment on table work is '작품';
comment on column work.id is '작품 식별키';
comment on column work.title is '작품명';
comment on column work.description is '작품 설명';
comment on column work.serial_state is '연재 상태';
comment on column work.thumbnail_id is '썸네일 이미지 식별키';
comment on column work.visibility is '노출 여부';
comment on column work.created_at is '생성일시';
comment on column work.modified_at is '수정일시';
