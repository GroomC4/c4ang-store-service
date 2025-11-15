package com.groom.store.application.service

import com.groom.store.application.dto.DeleteStoreCommand
import com.groom.store.common.annotation.UnitTest
import com.groom.store.common.enums.StoreStatus
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.model.Store
import com.groom.store.domain.port.LoadStorePort
import com.groom.store.domain.port.PublishEventPort
import com.groom.store.domain.port.SaveStorePort
import com.groom.store.domain.service.StoreAuditRecorder
import com.groom.store.domain.service.StoreManager
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
import java.util.Optional
import java.util.UUID

@UnitTest
class DeleteStoreServiceTest :
    BehaviorSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        Given("스토어 소유자가 자신의 스토어를 삭제하려고 할 때") {
            val loadStorePort = mockk<LoadStorePort>()
            val saveStorePort = mockk<SaveStorePort>()
            val publishEventPort = mockk<PublishEventPort>()
            val storeManager = mockk<StoreManager>()

            val deleteStoreService =
                DeleteStoreService(
                    loadStorePort = loadStorePort,
                    saveStorePort = saveStorePort,
                    publishEventPort = publishEventPort,
                    storeManager = storeManager,
                    storeAuditRecorder = mockk<StoreAuditRecorder>(relaxed = true),
                )

            val storeId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val command =
                DeleteStoreCommand(
                    storeId = storeId,
                    userId = userId,
                )

            val existingStore =
                StoreTestFixture.createStore(
                    id = storeId,
                    ownerUserId = userId,
                    name = "삭제할 스토어",
                    description = "삭제 예정",
                    status = StoreStatus.REGISTERED,
                )

            every { loadStorePort.loadById(storeId) } returns existingStore
            every { storeManager.deleteStore(existingStore, userId) } answers {
                existingStore.delete() // 불변 객체 패턴: DeleteResult 반환
            }
            every { saveStorePort.save(any<Store>()) } answers { firstArg() } // 불변 객체 패턴: 저장된 Store 반환
            every { publishEventPort.publishStoreDeleted(any()) } just runs

            When("스토어를 삭제하면") {
                val result = deleteStoreService.delete(command)

                Then("스토어가 성공적으로 삭제되고 결과가 반환된다") {
                    result shouldNotBe null
                    result.storeId shouldBe storeId.toString()
                    result.ownerUserId shouldBe userId.toString()
                    result.name shouldBe "삭제할 스토어"
                    result.deletedAt shouldNotBe null
                }
            }
        }

        Given("존재하지 않는 스토어를 삭제하려고 할 때") {
            val loadStorePort = mockk<LoadStorePort>()
            val saveStorePort = mockk<SaveStorePort>()
            val publishEventPort = mockk<PublishEventPort>()
            val storeManager = mockk<StoreManager>()

            val deleteStoreService =
                DeleteStoreService(
                    loadStorePort = loadStorePort,
                    saveStorePort = saveStorePort,
                    publishEventPort = publishEventPort,
                    storeManager = storeManager,
                    storeAuditRecorder = mockk<StoreAuditRecorder>(relaxed = true),
                )

            val storeId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val command =
                DeleteStoreCommand(
                    storeId = storeId,
                    userId = userId,
                )

            every { loadStorePort.loadById(storeId) } returns null

            When("스토어를 삭제하려고 하면") {
                Then("StoreException.StoreNotFound 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.StoreNotFound> {
                            deleteStoreService.delete(command)
                        }

                    exception.storeId shouldBe storeId
                }
            }
        }

        Given("스토어 소유자가 아닌 사용자가 스토어를 삭제하려고 할 때") {
            val loadStorePort = mockk<LoadStorePort>()
            val saveStorePort = mockk<SaveStorePort>()
            val publishEventPort = mockk<PublishEventPort>()
            val storeManager = mockk<StoreManager>()

            val deleteStoreService =
                DeleteStoreService(
                    loadStorePort = loadStorePort,
                    saveStorePort = saveStorePort,
                    publishEventPort = publishEventPort,
                    storeManager = storeManager,
                    storeAuditRecorder = mockk<StoreAuditRecorder>(relaxed = true),
                )

            val storeId = UUID.randomUUID()
            val ownerId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val command =
                DeleteStoreCommand(
                    storeId = storeId,
                    userId = userId,
                )

            val existingStore =
                StoreTestFixture.createStore(
                    id = storeId,
                    ownerUserId = ownerId,
                    name = "다른 사람 스토어",
                    description = "접근 불가",
                    status = StoreStatus.REGISTERED,
                )

            every { loadStorePort.loadById(storeId) } returns existingStore
            every { storeManager.deleteStore(existingStore, userId) } throws
                StoreException.StoreAccessDenied(storeId, userId)

            When("스토어를 삭제하려고 하면") {
                Then("StoreException.StoreAccessDenied 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.StoreAccessDenied> {
                            deleteStoreService.delete(command)
                        }

                    exception.storeId shouldBe storeId
                    exception.userId shouldBe userId
                }
            }
        }

        Given("이미 삭제된 스토어를 다시 삭제하려고 할 때") {
            val loadStorePort = mockk<LoadStorePort>()
            val saveStorePort = mockk<SaveStorePort>()
            val publishEventPort = mockk<PublishEventPort>()
            val storeManager = mockk<StoreManager>()

            val deleteStoreService =
                DeleteStoreService(
                    loadStorePort = loadStorePort,
                    saveStorePort = saveStorePort,
                    publishEventPort = publishEventPort,
                    storeManager = storeManager,
                    storeAuditRecorder = mockk<StoreAuditRecorder>(relaxed = true),
                )

            val storeId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val command =
                DeleteStoreCommand(
                    storeId = storeId,
                    userId = userId,
                )

            val deletedStore =
                StoreTestFixture
                    .createDeletedStore(
                        ownerUserId = userId,
                        name = "이미 삭제된 스토어",
                    ).apply {
                        StoreTestFixture.setField(this, "id", storeId)
                    }

            every { loadStorePort.loadById(storeId) } returns deletedStore
            every { storeManager.deleteStore(deletedStore, userId) } answers {
                deletedStore.delete()
            }

            When("스토어를 삭제하려고 하면") {
                Then("StoreException.StoreAlreadyDeleted 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.StoreAlreadyDeleted> {
                            deleteStoreService.delete(command)
                        }

                    exception.storeId shouldBe storeId
                }
            }
        }
    })
