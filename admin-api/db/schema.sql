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
    file_size   bigint      not null default 0,
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

--- 작품 회차
create sequence seq_episode start with 1 increment by 1 cache 50;

create table episode
(
    id           bigint primary key default nextval('seq_episode'),
    work_id      bigint references work,
    episode_no   integer   not null,
    thumbnail_id bigint references thumbnail_image,
    like_count   integer   not null default 0,
    view_count   integer   not null default 0,
    created_at   timestamp not null default current_timestamp,
    modified_at  timestamp not null default current_timestamp
);

create unique index uk_episode_work_id_episode_no on episode (work_id, episode_no);
create index idx_episode_work_id on episode (work_id);

comment on table episode is '작품 회차';
comment on column episode.id is '작품 회차 식별키';
comment on column episode.work_id is '작품 식별키';
comment on column episode.episode_no is '작품 회차 번호 (작품 식별키와 함께 uk)';
comment on column episode.thumbnail_id is '썸네일 이미지 식별키';
comment on column episode.like_count is '회차 좋아요 수';
comment on column episode.view_count is '회차 조회 수';
comment on column episode.created_at is '생성일시';
comment on column episode.modified_at is '수정일시';

--- 작품 회차별 이미지
create sequence seq_episode_image start with 1 increment by 1 cache 50;

create table episode_image
(
    id          bigint primary key default nextval('seq_episode_image'),
    episode_id  bigint references episode,
    sort_order  integer   not null,
    file_url    text      not null unique,
    file_size   bigint    not null default 0,
    created_at  timestamp not null default current_timestamp,
    modified_at timestamp not null default current_timestamp
);

create unique index uk_episode_image_episode_id_sort_order on episode_image (episode_id, sort_order);

comment on table episode_image is '작품 회차별 이미지';
comment on column episode_image.episode_id is '작품 회차 식별키';
comment on column episode_image.sort_order is '회차 내 이미지 정렬 순서 (회차 식별키와 함께 uk)';
comment on column episode_image.file_url is '이미지 파일 url';
comment on column episode_image.file_size is '이미지 파일 크기';
comment on column episode_image.created_at is '생성일시';
comment on column episode_image.modified_at is '수정일시';