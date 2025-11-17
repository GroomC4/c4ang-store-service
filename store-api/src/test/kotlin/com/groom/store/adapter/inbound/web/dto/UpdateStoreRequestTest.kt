package com.groom.store.adapter.inbound.web.dto

import com.groom.store.common.annotation.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * UpdateStoreRequest DTO 변환 로직 단위 테스트
 *
 * Request DTO가 Application Layer의 Command로 올바르게 변환되는지 검증합니다.
 */
@UnitTest
@DisplayName("UpdateStoreRequest DTO 변환 테스트")
class UpdateStoreRequestTest {
    @Test
    @DisplayName("toCommand() - 모든 필드가 있는 요청을 Command로 변환한다")
    fun testToCommand_WithAllFields() {
        // given
        val storeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val request =
            UpdateStoreRequest(
                name = "수정된 스토어",
                description = "수정된 설명",
            )

        // when
        val command = request.toCommand(storeId, userId)

        // then
        assertThat(command.storeId).isEqualTo(storeId)
        assertThat(command.userId).isEqualTo(userId)
        assertThat(command.name).isEqualTo("수정된 스토어")
        assertThat(command.description).isEqualTo("수정된 설명")
    }

    @Test
    @DisplayName("toCommand() - 설명이 null인 요청을 Command로 변환한다")
    fun testToCommand_WithoutDescription() {
        // given
        val storeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val request =
            UpdateStoreRequest(
                name = "수정된 스토어",
                description = null,
            )

        // when
        val command = request.toCommand(storeId, userId)

        // then
        assertThat(command.storeId).isEqualTo(storeId)
        assertThat(command.userId).isEqualTo(userId)
        assertThat(command.name).isEqualTo("수정된 스토어")
        assertThat(command.description).isNull()
    }

    @Test
    @DisplayName("toCommand() - 이름만 변경하는 요청을 Command로 변환한다")
    fun testToCommand_OnlyNameChange() {
        // given
        val storeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val request =
            UpdateStoreRequest(
                name = "새로운 이름",
                description = null,
            )

        // when
        val command = request.toCommand(storeId, userId)

        // then
        assertThat(command.storeId).isEqualTo(storeId)
        assertThat(command.userId).isEqualTo(userId)
        assertThat(command.name).isEqualTo("새로운 이름")
        assertThat(command.description).isNull()
    }

    @Test
    @DisplayName("toCommand() - 빈 문자열 설명을 가진 요청을 Command로 변환한다")
    fun testToCommand_WithEmptyDescription() {
        // given
        val storeId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val request =
            UpdateStoreRequest(
                name = "수정된 스토어",
                description = "",
            )

        // when
        val command = request.toCommand(storeId, userId)

        // then
        assertThat(command.storeId).isEqualTo(storeId)
        assertThat(command.userId).isEqualTo(userId)
        assertThat(command.name).isEqualTo("수정된 스토어")
        assertThat(command.description).isEqualTo("")
    }

    @Test
    @DisplayName("toCommand() - 동일한 사용자가 여러 스토어를 수정하는 요청을 Command로 변환한다")
    fun testToCommand_SameUserMultipleStores() {
        // given
        val storeId1 = UUID.randomUUID()
        val storeId2 = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val request1 =
            UpdateStoreRequest(
                name = "스토어 1",
                description = "설명 1",
            )
        val request2 =
            UpdateStoreRequest(
                name = "스토어 2",
                description = "설명 2",
            )

        // when
        val command1 = request1.toCommand(storeId1, userId)
        val command2 = request2.toCommand(storeId2, userId)

        // then
        assertThat(command1.userId).isEqualTo(userId)
        assertThat(command2.userId).isEqualTo(userId)
        assertThat(command1.storeId).isNotEqualTo(command2.storeId)
        assertThat(command1.name).isEqualTo("스토어 1")
        assertThat(command2.name).isEqualTo("스토어 2")
    }
}
