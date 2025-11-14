package com.groom.store.application.service

import com.groom.ecommerce.common.annotation.UnitTest
import com.groom.ecommerce.common.domain.DomainEventPublisher
import com.groom.ecommerce.common.exception.StoreException
import com.groom.store.application.dto.UpdateStoreCommand
import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import com.groom.store.domain.service.StorePolicy
import com.groom.store.fixture.StoreTestFixture
import com.groom.store.outbound.repository.StoreRepositoryImpl
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
import java.util.Optional
import java.util.UUID

@UnitTest
class UpdateStoreServiceTest :
    BehaviorSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        Given("스토어 소유자가 자신의 스토어를 수정하려고 할 때") {
            val storeRepository = mockk<StoreRepositoryImpl>()
            val storePolicy = mockk<StorePolicy>()
            val domainEventPublisher = mockk<DomainEventPublisher>()

            val updateService =
                UpdateService(
                    storeRepository = storeRepository,
                    storePolicy = storePolicy,
                    domainEventPublisher = domainEventPublisher,
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

            every { storeRepository.findById(storeId) } returns Optional.of(existingStore)
            every { storeRepository.save(any<Store>()) } answers { firstArg() }
            every { domainEventPublisher.publish(any()) } just runs

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
            val storeRepository = mockk<StoreRepositoryImpl>()
            val storePolicy = mockk<StorePolicy>()
            val domainEventPublisher = mockk<DomainEventPublisher>()

            val updateService =
                UpdateService(
                    storeRepository = storeRepository,
                    storePolicy = storePolicy,
                    domainEventPublisher = domainEventPublisher,
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
            every { storeRepository.findById(storeId) } returns Optional.empty()

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
            val storeRepository = mockk<StoreRepositoryImpl>()
            val storePolicy = mockk<StorePolicy>()
            val domainEventPublisher = mockk<DomainEventPublisher>()

            val updateService =
                UpdateService(
                    storeRepository = storeRepository,
                    storePolicy = storePolicy,
                    domainEventPublisher = domainEventPublisher,
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
            val storeRepository = mockk<StoreRepositoryImpl>()
            val storePolicy = mockk<StorePolicy>()
            val domainEventPublisher = mockk<DomainEventPublisher>()

            val updateService =
                UpdateService(
                    storeRepository = storeRepository,
                    storePolicy = storePolicy,
                    domainEventPublisher = domainEventPublisher,
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

            every { storeRepository.findById(storeId) } returns Optional.of(existingStore)
            every { storeRepository.save(any<Store>()) } answers {
                // 불변 객체 패턴: 저장된 Store 반환 (updatedAt 설정)
                val store = firstArg<Store>()
                val updatedAtField =
                    store.javaClass.superclass.getDeclaredField("updatedAt").apply {
                        isAccessible = true
                    }
                updatedAtField.set(store, LocalDateTime.now())
                store
            }
            every { domainEventPublisher.publish(any()) } just runs

            When("스토어를 수정하면") {
                val result = updateService.update(command)

                Then("description이 null로 수정된다") {
                    result shouldNotBe null
                    result.description shouldBe null
                    result.name shouldBe command.name

                    verify(exactly = 1) { storePolicy.checkStoreAccess(storeId, userId) }
                    verify(exactly = 1) { storeRepository.findById(storeId) }
                    verify(exactly = 1) { storeRepository.save(any<Store>()) } // 불변 객체 패턴
                    verify(exactly = 1) { domainEventPublisher.publish(any()) }
                }
            }
        }
    })
