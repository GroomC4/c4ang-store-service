# ========================
# Build Stage
# ========================
FROM gradle:8.5-jdk21 AS build

# GitHub Packages 인증을 위한 ARG (CI/CD에서 자동 전달)
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

# 환경 변수로 설정 (Gradle이 사용)
ENV GITHUB_ACTOR=${GITHUB_ACTOR}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

WORKDIR /app

# Gradle 캐싱 최적화를 위해 의존성 파일만 먼저 복사
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY gradlew ./

# 서브모듈 build.gradle.kts 복사
COPY store-api/build.gradle.kts store-api/

# 의존성 다운로드 (캐싱 활용)
RUN ./gradlew dependencies --no-daemon || true

# 전체 소스 코드 복사
COPY . .

# Gradle 빌드 (테스트 제외)
RUN ./gradlew clean build -x test --no-daemon

# ========================
# Runtime Stage
# ========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /app/store-api/build/libs/*.jar app.jar

# 애플리케이션 실행을 위한 메타데이터
LABEL maintainer="c4ang-store-service"
LABEL description="C4ang Store Service - 스토어 관리 마이크로서비스"

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
