# my-pocket-webtoon-admin

[my-pocket-webtoon](https://github.com/GuardJo/my-pocket-webtoon) 서비스에 대한 어드민 서비스

> `my-pocket-webtoon`  내 작품 및 회원 관리를 위한 백오피스
>

# 인프라 구성

- DB : supabase (postgreSQL)
- 파일 스토리지 : cloudflare R2

# 모듈 구성

- spring boot 기반 (jdk 17)
- JPA (hibernate)
- JWT 토큰 기반 인증/인가 처리

# DB 스키마 구성

```mermaid
---
title: my-pocket-webtoon
---
erDiagram
    admin_info {
varchar(20) id pk "관리자 아이디"
varchar(50) name "이름"
varchar(300) password "비밀번호"
bool activate "활성 상태"
varchar(20) role_id fk "권한 식별키"
timestamp created_at "생성일자"
timestamp modified_at "수정일자"
}

admin_role {
varchar(30) id pk "권한 식별키"
varchar(100) description "권한 설명"
bool activate "활성상태"
timestamp created_at "생성일자"
timestamp modified_at "수정일자"
}
user_info {
varchar(20) id pk "회원 아이디"
varchar(50) name "회원명"
varchar(100) nickname uk "회원 닉네임"
varchar(300) password "비밀번호"
bool activate "활성 상태"
varchar(8) birth_ymd "생년월일"
varchar(20) register_admin_id fk "가입 승인 관리자 아이디"
timestamp created_at "생성일자"
timestamp modified_at "수정일자"
}

work {
bigint id pk "작품 식별키"
varchar(100) title uk "작품명"
varchar(500) description "작품 설명"
varchar(10) serial_state "연재 상태 (not null)"
bigint thumbnail_id fk "썸네일 이미지 식별키"
bool visibility "노출 여부 (not null)"
timestamp created_at "not null"
timestamp modified_at "not null"
}
episode {
bigint id pk "작품 회차 식별키"
bigint work_id fk "작품 식별키"
integer episode_no uk "작품 회차 번호(작품 식별키와 함께 UK)"
bigint thumbnail_id fk "작품 회차 썸네일 이미지 식별키"
integer like_count "회차 좋아요 수"
bigint view_count "회차 조회 수"
timestamp created_at "not null"
timestamp modified_at "not null"
}
episode_image {
bigint id pk "작품 회차별 이미지 식별키"
bigint episode_id fk "작품 회차 식별키"
integer sort_order uk "작품 회차 내 이미지 정렬 순서 (episode_id 와 uk)"
text file_url uk "이미지 파일 url"
integer file_size "이미지 파일 크기"
timestamp created_at "not null"
timestamp modified_at "not null"
}
thumbnail_image {
bigint id pk "썸네일 이미지 식별키"
text file_url uk "이미지 파일 url"
integer file_size "이미지 파일 크기"
timestamp created_at "not null"
timestamp modified_at "not null"
}

reading_history {
bigint id pk "작품 조회 이력 식별키"
bigint work_id fk, uk "작품 식별키 (회원과 uk)"
varchar(20) user_id fk, uk "회원 식별키 (작품과 uk)"
integer episode_id fk "회차 식별키"
timestamp created_at "생성일자"
timestamp modified_at "수정일자"
}

work ||--|{ episode: "work_id"
work ||--|| thumbnail_image: "thumbnail_id"
episode ||--|| thumbnail_image: "thumbnail_id"
episode ||--|{ episode_image: "episode_id"

user_info }o--|| admin_info: "register_admin_id"

admin_info ||--|| admin_role: "role_id"

reading_history }|--|| work: "work_id"
reading_history }|--|| user_info: "user_id"
reading_history }|--|| episode: "episode_id"
```