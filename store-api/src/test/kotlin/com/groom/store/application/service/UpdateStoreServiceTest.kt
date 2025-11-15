package com.groom.store.application.service

import com.groom.store.application.dto.UpdateStoreCommand
import com.groom.store.common.annotation.UnitTest
import com.groom.store.common.enums.StoreStatus
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.model.Store
import com.groom.store.domain.port.LoadStorePort
import com.groom.store.domain.port.PublishEventPort
import com.groom.store.domain.port.SaveStorePort
import com.groom.store.domain.service.StoreAuditRecorder
import com.groom.store.domain.service.StorePolicy
import com.groom.store.fixture.StoreTestFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.LocalDateTime
import java.util.UUID

@UnitTest
class UpdateStoreServiceTest :
    BehaviorSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        Given("스토어 소유자가 자신의 스토어를 수정하려고 할 때") {
            val loadStorePort = mockk<LoadStorePort>()
            val saveStorePort = mockk<SaveStorePort>()
            val publishEventPort = mockk<PublishEventPort>()
            val storePolicy = mockk<StorePolicy>()

            val updateService =
                UpdateService(
                    loadStorePort = loadStorePort,
                    saveStorePort = saveStorePort,
                    publishEventPort = publishEventPort,
                    storePolicy = storePolicy,
                    storeAuditRecorder = mockk<StoreAuditRecorder>(relaxed = true),
                )

            val storeId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val command =
                UpdateStoreCommand(
                    storeId = storeId,
                    userId = userId,
                    name = "수정된 스토어",
                    description = "수정된 설명",
                )

            every { storePolicy.checkStoreAccess(storeId, userId) } just runs

            // 기존 Store 객체
            val existingStore =
                StoreTestFixture.createStore(
                    id = storeId,
                    ownerUserId = userId,
                    name = "기존 스토어",
                    description = "기존 설명",
                    status = StoreStatus.REGISTERED,
                    updatedAt = LocalDateTime.now(),
                )

            every { loadStorePort.loadById(storeId) } returns existingStore
            every { saveStorePort.save(any<Store>()) } answers { firstArg() }
            every { publishEventPort.publishStoreInfoUpdated(any()) } just runs

            When("스토어를 수정하면") {
                val result = updateService.update(command)

                Then("스토어 정보가 성공적으로 수정되고 결과가 반환된다") {
                    result shouldNotBe null
                    result.storeId shouldBe storeId.toString()
                    result.ownerUserId shouldBe userId.toString()
                    result.name shouldBe command.name
                    result.description shouldBe command.description
                    result.status shouldBe "REGISTERED"
                    result.updatedAt shouldNotBe null

                    // 결과 상태로 충분히 검증하므로 verify 불필요
                }
            }
        }

        Given("존재하지 않는 스토어를 수정하려고 할 때") {
            val loadStorePort = mockk<LoadStorePort>()
            val saveStorePort = mockk<SaveStorePort>()
            val publishEventPort = mockk<PublishEventPort>()
            val storePolicy = mockk<StorePolicy>()

            val updateService =
                UpdateService(
                    loadStorePort = loadStorePort,
                    saveStorePort = saveStorePort,
                    publishEventPort = publishEventPort,
                    storePolicy = storePolicy,
                    storeAuditRecorder = mockk<StoreAuditRecorder>(relaxed = true),
                )

            val storeId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val command =
                UpdateStoreCommand(
                    storeId = storeId,
                    userId = userId,
                    name = "수정된 스토어",
                    description = "수정된 설명",
                )

            every { storePolicy.checkStoreAccess(storeId, userId) } just runs
            every { loadStorePort.loadById(storeId) } returns null

            When("스토어를 수정하려고 하면") {
                Then("StoreException.StoreNotFound 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.StoreNotFound> {
                            updateService.update(command)
                        }

                    exception.storeId shouldBe storeId

                    // 예외 발생으로 충분히 검증하므로 verify 불필요
                }
            }
        }

        Given("스토어 소유자가 아닌 사용자가 스토어를 수정하려고 할 때") {
            val loadStorePort = mockk<LoadStorePort>()
            val saveStorePort = mockk<SaveStorePort>()
            val publishEventPort = mockk<PublishEventPort>()
            val storePolicy = mockk<StorePolicy>()

            val updateService =
                UpdateService(
                    loadStorePort = loadStorePort,
                    saveStorePort = saveStorePort,
                    publishEventPort = publishEventPort,
                    storePolicy = storePolicy,
                    storeAuditRecorder = mockk<StoreAuditRecorder>(relaxed = true),
                )

            val storeId = UUID.randomUUID()
            val ownerId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val command =
                UpdateStoreCommand(
                    storeId = storeId,
                    userId = userId,
                    name = "수정된 스토어",
                    description = "수정된 설명",
                )

            every { storePolicy.checkStoreAccess(storeId, userId) } throws
                StoreException.StoreAccessDenied(storeId, userId)

            When("스토어를 수정하려고 하면") {
                Then("StoreException.StoreAccessDenied 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.StoreAccessDenied> {
                            updateService.update(command)
                        }

                    exception.storeId shouldBe storeId
                    exception.userId shouldBe userId

                    // 예외 발생으로 충분히 검증하므로 verify 불필요
                }
            }
        }

        Given("description을 null로 수정하려고 할 때") {
            val loadStorePort = mockk<LoadStorePort>()
            val saveStorePort = mockk<SaveStorePort>()
            val publishEventPort = mockk<PublishEventPort>()
            val storePolicy = mockk<StorePolicy>()

            val updateService =
                UpdateService(
                    loadStorePort = loadStorePort,
                    saveStorePort = saveStorePort,
                    publishEventPort = publishEventPort,
                    storePolicy = storePolicy,
                    storeAuditRecorder = mockk<StoreAuditRecorder>(relaxed = true),
                )

            val storeId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val command =
                UpdateStoreCommand(
                    storeId = storeId,
                    userId = userId,
                    name = "수정된 스토어",
                    description = null,
                )

            every { storePolicy.checkStoreAccess(storeId, userId) } just runs

            val existingStore =
                Store(
                    ownerUserId = userId,
                    name = "기존 스토어",
                    description = "기존 설명",
                    status = StoreStatus.REGISTERED,
                ).apply {
                    id = storeId
                    // updatedAt 설정 (리플렉션 사용)
                    val updatedAtField =
                        this.javaClass.superclass.getDeclaredField("updatedAt").apply {
                            isAccessible = true
                        }
                    updatedAtField.set(this, LocalDateTime.now())
                }

            every { loadStorePort.loadById(storeId) } returns existingStore
            every { saveStorePort.save(any<Store>()) } answers {
                // 불변 객체 패턴: 저장된 Store 반환 (updatedAt 설정)
                val store = firstArg<Store>()
                val updatedAtField =
                    store.javaClass.superclass.getDeclaredField("updatedAt").apply {
                        isAccessible = true
                    }
                updatedAtField.set(store, LocalDateTime.now())
                store
            }
            every { publishEventPort.publishStoreInfoUpdated(any()) } just runs

            When("스토어를 수정하면") {
                val result = updateService.update(command)

                Then("description이 null로 수정된다") {
                    result shouldNotBe null
                    result.description shouldBe null
                    result.name shouldBe command.name

                    verify(exactly = 1) { storePolicy.checkStoreAccess(storeId, userId) }
                    verify(exactly = 1) { loadStorePort.loadById(storeId) }
                    verify(exactly = 1) { saveStorePort.save(any<Store>()) } // 불변 객체 패턴
                    verify(exactly = 1) { publishEventPort.publishStoreInfoUpdated(any()) }
                }
            }
        }
    })
