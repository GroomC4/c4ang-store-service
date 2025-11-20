package com.groom.store.config

import com.groom.store.adapter.out.client.UserServiceFeignClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * FeignClient 테스트 설정
 *
 * Spring Cloud Contract Stub Runner 테스트를 위한 FeignClient 설정입니다.
 * Stub Runner가 실행하는 WireMock 서버에 연결하도록 설정합니다.
 */
@TestConfiguration
@EnableFeignClients(basePackages = ["com.groom.store.adapter.out.client"])
class FeignClientTestConfiguration {

    // Stub Runner가 8090 포트에서 WireMock을 실행하므로
    // FeignClient가 해당 포트를 사용하도록 설정이 필요합니다.
    // application-test.yml에서 설정하거나 @TestPropertySource를 사용할 수 있습니다.
}