# base 이미지 설정
FROM openjdk:17-jdk-slim

# JAR 파일을 Docker 컨테이너로 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]