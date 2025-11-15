package com.groom.store.common.extension

import com.groom.platform.testSupport.BaseContainerExtension
import java.io.File

/**
 * Store Service용 통합 테스트 컨테이너 Extension
 *
 * c4ang-platform-core의 BaseContainerExtension을 상속받아 Store Service에 필요한
 * Docker Compose 파일과 스키마 파일 경로를 제공합니다.
 */
class SharedContainerExtension : BaseContainerExtension() {
    override fun getComposeFile(): File = resolveComposeFile("c4ang-platform-core/docker-compose/test/docker-compose-integration-test.yml")

    override fun getSchemaFile(): File {
        // Store Service의 PostgreSQL 스키마 파일
        return resolveComposeFile("store-api/sql/schema.sql")
    }
}
