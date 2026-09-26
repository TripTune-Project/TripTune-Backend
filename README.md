# TripTune - Backend

TripTune-Backend는 여러 사용자가 함께 여행계획을 작성하고 여행지 정보를 얻을 수 있도록 지원하는 웹 서비스의 백엔드입니다.

---
## 🛠 기술 스택
- **Language**: Java 17
- **Framework**: Spring Boot 3.1.11
- **Library**: Spring Data JPA, QueryDSL, Actuator
- **Database**: MySQL, Redis, MongoDB
- **Testing**: JUnit, Mockito
- **Load Testing**: K6
- **Monitoring**: Prometheus, Grafana
- **Infra**: AWS EC2, S3, Docker, GitHub Actions

---
## ✨ 주요 기능

### 사용자 인증 및 계정 관리
- JWT 기반 로그인 및 인증/인가
- 이메일 인증을 통한 회원가입 및 비밀번호 변경
- OAuth2를 이용한 간편 로그인
- 사용자 프로필 및 프로필 이미지 관리

### 여행 일정
- 여행 일정 생성, 수정 및 삭제
- 일정에 참여할 사용자 초대 및 관리
- 일정별 여행 루트 생성, 수정 및 삭제

### 실시간 채팅
- STOMP 기반 실시간 채팅
- 여행 일정 참여자 간 채팅
- MongoDB를 이용한 채팅 메시지 저장

### 여행지
- 여행지 검색 및 조회
- 여행지 북마크

---

## 📂 파일 구조

```
TripTune-Backend
├── src/
│   ├── main/
│   │   ├── java/com/triptune/
│   │   │   ├── bookmark/                 # 북마크 관련 기능
│   │   │   ├── common/                   # 공통 기능
│   │   │   ├── email/                    # 이메일 인증 기능
│   │   │   ├── global/                   # 공통 설정 기능
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
│   │   │   ├── member/                   # 사용자 관련 기능
│   │   │   ├── profile/                  # 프로필 이미지 관련 기능
│   │   │   ├── schedule/                 # 일정 정보 관련 기능
│   │   │   └── travel/                   # 여행지 관련 기능
│   │   └── resources/
│   │       ├── static/images/            # 이미지 파일
│   │       ├── templates/                # 이메일 인증 관련 HTML 템플릿 파일
│   │       ├── application.yml           # 애플리케이션 설정
│   │       └── application-prod.yml      # 운영 애플리케이션 설정    
│   ├── test/                             # 단위 및 통합 테스트 코드
├── k6/
│   ├── api/                              # K6 API 요청 테스트 스크립트 (.js)
│   └── scripts/                          # 테스트 실행 쉘 스크립트 (.sh)
├── Dockerfile                            # Docker 이미지 빌드 설정
└── build.gradle                          # Gradle 빌드 설정
```
- 도메인 패키지 구조: `member`, `schedule` 등 각 도메인 패키지 내부에 `controller`, `service`, `repository`, `entity`, `enums`, `dto` 하위 패키지를 각각 구성하여 도메인별 응집도를 높이고 유지보수성을 향상시켰습니다.

---
## 🏗️ 서비스 아키텍처

<img width="1451" height="1246" alt="Image" src="https://github.com/user-attachments/assets/f83fd861-4741-487b-9780-a1088f1320e7" />

---
## 📊 ERD 설계

<img width="2432" height="1343" alt="Image" src="https://github.com/user-attachments/assets/e326ef9b-1e1d-4913-819e-785395dde958" />

---
## 🔐 환경 변수

실행에 필요한 환경 변수는 `src/main/resources/.env` 파일에 설정합니다.

| 환경 변수                           | 설명                        |
|---------------------------------|---------------------------|
| `MYSQL_HOST`                    | MySQL 서버 주소               |
| `MYSQL_PORT`                    | MySQL 포트                  |
| `MYSQL_USERNAME`                | MySQL 사용자명                |
| `MYSQL_PASSWORD`                | MySQL 비밀번호                |
| `MONGODB_URI`                   | MongoDB Atlas 접속 주소       |
| `REDIS_HOST`                    | Redis 서버 주소               |
| `REDIS_PORT`                    | Redis 포트                  |
| `MAIL_USERNAME`                 | Gmail SMTP 사용자명           |
| `MAIL_PASSWORD`                 | Gmail SMTP 비밀번호           |
| `JWT_SECRET`                    | JWT 인증 키                  |
| `NAVER_CLIENT_ID`               | Naver OAuth 클라이언트 ID      |
| `NAVER_PASSWORD`                | Naver OAuth 클라이언트 시크릿     |
| `NAVER_REDIRECT_URI`            | Naver OAuth 인증 후 리다이렉트 주소 |
| `KAKAO_CLIENT_ID`               | Kakao OAuth 클라이언트 ID      |
| `KAKAO_CLIENT_SECRET`           | Kakao OAuth 클라이언트 시크릿     |
| `KAKAO_REDIRECT_URI`            | Kakao OAuth 인증 후 리다이렉트 주소 |
| `S3_BUCKET`                     | S3 버킷명                    |
| `S3_BASE_URL`                   | S3 기본 URL                 |
| `AWS_ACCESS_KEY`                | AWS 액세스 키                 |
| `AWS_SECRET_KEY`                | AWS 시크릿 키                 |
| `DEFAULT_PROFILE_S3_OBJECT_URL` | 사용자 기본 프로필 이미지 S3 객체 URL  |
| `DEFAULT_PROFILE_S3_OBJECT_KEY` | 사용자 기본 프로필 이미지 S3 객체 키    |
| `DEFAULT_PROFILE_ORIGINAL_NAME` | 사용자 기본 프로필 이미지 원본 파일명     |
| `DEFAULT_PROFILE_FILE_NAME`     | 사용자 기본 프로필 이미지 파일명        |
| `DEFAULT_PROFILE_EXTENSION`     | 사용자 기본 프로필 이미지 파일 확장자     |
| `DEFAULT_PROFILE_SIZE`          | 사용자 기본 프로필 이미지 파일 크기      |
| `CORS_ALLOWED_ORIGIN_1`         | CORS 허용 Origin            |
| `CORS_ALLOWED_ORIGIN_2`         | CORS 허용 Origin            |
| `CORS_ALLOWED_ORIGIN_3`         | CORS 허용 Origin            |
| `CORS_ALLOWED_ORIGIN_4`         | CORS 허용 Origin            |



---
## 🚀 실행 방법

### 1. 저장소 클론
```commandline
git clone <repository-url>
cd TripTune-Backend
```

### 2. 환경 변수 설정
프로젝트의 src/main/resources 경로에 .env 파일을 생성하고 필요한 환경 변수를 설정합니다.

환경 변수 목록은 [환경 변수](#-환경-변수) 항목을 참고합니다.


### 3. 프로젝트 실행
`prod` 프로파일을 활성화하여 Spring Boot 애플리케이션을 실행합니다.

```commandline
./gradlew bootRun --args='--spring.profiles.active=prod'
```
Windows:
```commandline
gradlew.bat bootRun --args="--spring.profiles.active=prod"
```

---
## 🔄 CI/CD 파이프라인

<img width="1462" height="618" alt="Image" src="https://github.com/user-attachments/assets/b5dc11f2-296e-4202-97ed-c44c5e208cb0" />
