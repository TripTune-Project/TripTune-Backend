# base 이미지 설정
FROM openjdk:17-jdk-slim

# JAR 파일을 Docker 컨테이너로 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "-javaagent:/pinpoint-agent-3.0.1/pinpoint-bootstrap-3.0.1.jar", "-Dpinpoint.agentId=appDev","-Dpinpoint.applicationName=app1","-Dpinpoint.config=/pinpoint-agent-3.0.1/pinpoint-root.config", "-Duser.timezone=Asia/Seoul", "/app.jar"]
