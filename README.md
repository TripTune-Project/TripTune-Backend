# TripTune - Backend

TripTune-Backend는 여러 사용자가 함께 여행계획을 작성하고 여행지 정보를 얻을 수 있도록 지원하는 웹 서비스의 백엔드입니다.

---
## 🛠 기술 스택
- **Language**: Java 17
- **Framework**: Spring Boot 3.1.11
- **Library** : Spring Data JPA, QueryDSL, K6, Actuator, Grafana, Prometheus
- **Database**: MySQL, Redis, MongoDB
- **Infra**: AWS EC2, S3, Docker, GitHub Actions

---
## ✨ 주요 기능
- 실시간 공동 여행 일정 작성 및 관리
- 여행지 검색 및 추천
- 채팅을 통한 여행 계획 논의
- 일정 공유 및 초대
- 사용자 인증 및 계정 관리

---

## 📂 파일 구조

```
TripTune-Backend
├── src/
│   ├── main/
│   │   ├── java/com/triptune/
│   │   │   ├── bookmark/                 # 북마크 관련 API
│   │   │   ├── common/                   # 공통 기능
│   │   │   ├── email/                    # 이메일 인증 API
│   │   │   ├── global/                   # API 공통 설정
│   │   │   │   ├── aop/                  # 공통 부가 기능 (권한체크, 여행지 존재 여부 체크)
│   │   │   │   ├── config/               # 스프링 빈 및 외부 라이브러리 설정 (이메일, QueryDSL, Swagger)
│   │   │   │   ├── exception/            # 커스텀 예외 및 전역 예외 처리
│   │   │   │   ├── message/              # 메시지 소스 (에러/성공 메시지 관리)
│   │   │   │   ├── redis/                # Redis 설정 및 연동
│   │   │   │   ├── response/             # 공통 API 응답 포맷
│   │   │   │   ├── s3/                   # AWS S3 설정 및 파일 업로드/다운로드 연동
│   │   │   │   ├── security/             # Spring Security 및 JWT 인증/인가
│   │   │   │   ├── util/                 # 공통 유틸리티 클래스
│   │   │   │   ├── validation/           # 커스텀 데이터 검증(Validation) 어노테이션
│   │   │   │   └── websocket/            # 실시간 통신(WebSocket) 설정
│   │   │   ├── member/                   # 사용자 관련 API
│   │   │   ├── profile/                  # 프로필 이미지 관련 API
│   │   │   ├── schedule/                 # 일정 정보 관련 API
│   │   │   └── travel/                   # 여행지 관련 API
│   │   └──resources/
│   │      ├── static/images/             # 이미지 파일
│   │      ├── templates/                 # 이메일 인증 관련 HTML 템플릿 파일 
│   │      └── application.yml            # 애플리케이션 설정     
│   ├── test/                             # 단위 및 통합 테스트 코드
├── k6/
│   ├── api/                              # K6 API 요청 테스트 스크립트 (.js)
│   └── scripts/                          # 테스트 실행 쉘 스크립트 (.sh)
├── Dockerfile                            # Docker 이미지 빌드 설정
└── build.gradle                          # Gradle 빌드 설정
```
- 도메인 패키지 구조 : `member`, `schedule` 등 각 도메인 패키지 내부에 `controller`, `service`, `repository`, `entity`, `enums`, `dto` 하위 패키지를 각각 구성하여 도메인 간의 응집도를 높이고 유지보수성을 향상시켰습니다.



