package com.groom.store.adapter.out.persistence

import com.groom.store.common.config.TestDataSourceConfig
import com.groom.store.common.extension.SharedContainerExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * Repository 테스트를 위한 기본 클래스
 *
 * SharedContainerExtension을 통해 PostgreSQL Primary/Replica와 Redis를 설정합니다.
 * TestDataSourceConfig를 통해 데이터 소스를 구성합니다.
 * 모든 Repository 테스트 클래스는 이 클래스를 상속받아야 합니다.
 */
@DataJpaTest
@ExtendWith(SharedContainerExtension::class)
@Import(TestDataSourceConfig::class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class BaseRepositoryTest
