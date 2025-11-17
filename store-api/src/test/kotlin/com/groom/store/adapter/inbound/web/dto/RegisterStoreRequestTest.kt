package com.groom.store.adapter.inbound.web.dto

import com.groom.store.common.annotation.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * RegisterStoreRequest DTO 변환 로직 단위 테스트
 *
 * Request DTO가 Application Layer의 Command로 올바르게 변환되는지 검증합니다.
 */
@UnitTest
@DisplayName("RegisterStoreRequest DTO 변환 테스트")
class RegisterStoreRequestTest {
    @Test
    @DisplayName("toCommand() - 모든 필드가 있는 요청을 Command로 변환한다")
    fun testToCommand_WithAllFields() {
        // given
        val ownerUserId = UUID.randomUUID()
        val request =
            RegisterStoreRequest(
                name = "테스트 스토어",
                description = "테스트 설명",
            )

        // when
        val command = request.toCommand(ownerUserId)

        // then
        assertThat(command.ownerUserId).isEqualTo(ownerUserId)
        assertThat(command.name).isEqualTo("테스트 스토어")
        assertThat(command.description).isEqualTo("테스트 설명")
    }

    @Test
    @DisplayName("toCommand() - 설명이 null인 요청을 Command로 변환한다")
    fun testToCommand_WithoutDescription() {
        // given
        val ownerUserId = UUID.randomUUID()
        val request =
            RegisterStoreRequest(
                name = "테스트 스토어",
                description = null,
            )

        // when
        val command = request.toCommand(ownerUserId)

        // then
        assertThat(command.ownerUserId).isEqualTo(ownerUserId)
        assertThat(command.name).isEqualTo("테스트 스토어")
        assertThat(command.description).isNull()
    }

    @Test
    @DisplayName("toCommand() - 빈 문자열 설명을 가진 요청을 Command로 변환한다")
    fun testToCommand_WithEmptyDescription() {
        // given
        val ownerUserId = UUID.randomUUID()
        val request =
            RegisterStoreRequest(
                name = "테스트 스토어",
                description = "",
            )

        // when
        val command = request.toCommand(ownerUserId)

        // then
        assertThat(command.ownerUserId).isEqualTo(ownerUserId)
        assertThat(command.name).isEqualTo("테스트 스토어")
        assertThat(command.description).isEqualTo("")
    }

    @Test
    @DisplayName("toCommand() - 긴 이름과 설명을 가진 요청을 Command로 변환한다")
    fun testToCommand_WithLongValues() {
        // given
        val ownerUserId = UUID.randomUUID()
        val longName = "A".repeat(100)
        val longDescription = "B".repeat(500)
        val request =
            RegisterStoreRequest(
                name = longName,
                description = longDescription,
            )

        // when
        val command = request.toCommand(ownerUserId)

        // then
        assertThat(command.ownerUserId).isEqualTo(ownerUserId)
        assertThat(command.name).isEqualTo(longName)
        assertThat(command.description).isEqualTo(longDescription)
    }

    @Test
    @DisplayName("toCommand() - 특수문자를 포함한 요청을 Command로 변환한다")
    fun testToCommand_WithSpecialCharacters() {
        // given
        val ownerUserId = UUID.randomUUID()
        val request =
            RegisterStoreRequest(
                name = "테스트@#$% 스토어!",
                description = "설명 with special chars: <>?/\\|",
            )

        // when
        val command = request.toCommand(ownerUserId)

        // then
        assertThat(command.ownerUserId).isEqualTo(ownerUserId)
        assertThat(command.name).isEqualTo("테스트@#$% 스토어!")
        assertThat(command.description).isEqualTo("설명 with special chars: <>?/\\|")
    }
}
