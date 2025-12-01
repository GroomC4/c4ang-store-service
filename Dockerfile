# ========================
# Build Stage
# ========================
FROM gradle:8.5-jdk21 AS build

# GitHub Packages 인증을 위한 ARG
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

WORKDIR /app

# [Layer 1] Gradle Wrapper 복사 (거의 변경되지 않음)
COPY gradle gradle
COPY gradlew ./

# [Layer 2] 빌드 설정 파일 복사 (의존성 변경 시에만 캐시 무효화)
COPY build.gradle.kts settings.gradle.kts ./
COPY store-api/build.gradle.kts store-api/

# [Layer 3] gradle.properties 생성 및 의존성 다운로드 (단일 레이어로 통합)
# - 인증 설정과 의존성 다운로드를 함께 수행하여 안정성 향상
# - 의존성이 변경되지 않으면 이 레이어가 캐시됨
RUN mkdir -p ~/.gradle && \
    echo "gpr.user=${GITHUB_ACTOR}" >> ~/.gradle/gradle.properties && \
    echo "gpr.key=${GITHUB_TOKEN}" >> ~/.gradle/gradle.properties && \
    ./gradlew dependencies --no-daemon || true

# [Layer 4] 소스 코드 복사 (자주 변경됨 - 위 레이어 캐시 유지)
COPY store-api/src store-api/src

# [Layer 5] 애플리케이션 빌드
# - clean 제거: 새로운 컨테이너이므로 불필요
# - 테스트 제외: Docker 환경에서는 Testcontainers 사용 불가
RUN ./gradlew :store-api:build -x test -x contractTest -x integrationTest --no-daemon

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
  CMD wget --quiet --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# 애플리케이션 실행
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
