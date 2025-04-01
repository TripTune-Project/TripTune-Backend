# base 이미지 설정
FROM openjdk:17-jdk-slim

# Pinpoint Agent 폴더를 Docker 컨테이너로 복사
COPY pinpoint-agent /pinpoint-agent

# JAR 파일을 Docker 컨테이너로 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Spring Boot 애플리케이션 실행
ENTRYPOINT [ "java", 
    "-javaagent:/pinpoint-agent/pinpoint-bootstrap-3.0.1.jar",
    "-Dpinpoint.agentId=appDev",
    "-Dpinpoint.applicationName=app1",
    "-Dpinpoint.config=/pinpoint-agent/pinpoint-root.config",
    "-Duser.timezone=Asia/Seoul",
    "-jar",
    "/app.jar"
]

