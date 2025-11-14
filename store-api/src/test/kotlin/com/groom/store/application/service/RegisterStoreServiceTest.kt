package com.groom.store.application.service

import com.groom.store.application.dto.RegisterStoreCommand
import com.groom.store.application.service.RegisterService
import com.groom.store.common.annotation.UnitTest
import com.groom.store.common.enums.StoreStatus
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.port.PublishEventPort
import com.groom.store.domain.port.SaveStorePort
import com.groom.store.domain.service.SellerPolicy
import com.groom.store.domain.service.StoreFactory
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
import java.time.LocalDateTime
import java.util.UUID

@UnitTest
class RegisterStoreServiceTest :
    BehaviorSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        Given("유효한 스토어 등록 정보가 주어졌을 때") {
            val saveStorePort = mockk<SaveStorePort>()
            val sellerPolicy = mockk<SellerPolicy>()
            val storeFactory = mockk<StoreFactory>()
            val publishEventPort = mockk<PublishEventPort>()

            val registerService =
                RegisterService(
                    sellerPolicy = sellerPolicy,
                    storeFactory = storeFactory,
                    saveStorePort = saveStorePort,
                    publishEventPort = publishEventPort,
                )

            val ownerUserId = UUID.randomUUID()
            val command =
                RegisterStoreCommand(
                    ownerUserId = ownerUserId,
                    name = "테크 스토어",
                    description = "최신 전자제품 판매",
                )

            every { sellerPolicy.checkOwnerRole(ownerUserId) } just runs

            val testStoreId = UUID.randomUUID()
            val testCreatedAt = LocalDateTime.now()

            // StoreFactory가 반환할 Store 객체 (아직 ID 없음)
            val createdStore =
                StoreTestFixture.createStore(
                    ownerUserId = ownerUserId,
                    name = command.name,
                    description = command.description,
                    status = StoreStatus.REGISTERED,
                )

            every {
                storeFactory.createNewStore(
                    ownerUserId = ownerUserId,
                    name = command.name,
                    description = command.description,
                )
            } returns createdStore

            // Repository가 반환할 Store 객체 (ID와 createdAt 설정됨)
            val savedStore =
                StoreTestFixture.createStore(
                    id = testStoreId,
                    ownerUserId = ownerUserId,
                    name = command.name,
                    description = command.description,
                    status = StoreStatus.REGISTERED,
                    createdAt = testCreatedAt,
                )

            every { saveStorePort.save(any()) } returns savedStore
            every { publishEventPort.publishStoreCreated(any()) } just runs

            When("스토어를 등록하면") {
                val result = registerService.register(command)

                Then("스토어가 성공적으로 생성되고 결과가 반환된다") {
                    result shouldNotBe null
                    result.storeId shouldBe testStoreId.toString()
                    result.ownerUserId shouldBe ownerUserId.toString()
                    result.name shouldBe command.name
                    result.description shouldBe command.description
                    result.status shouldBe "REGISTERED"
                    result.createdAt shouldNotBe null

                    // 결과 상태로 충분히 검증하므로 verify 불필요
                }
            }
        }

        Given("이미 스토어를 보유한 사용자가 스토어를 등록하려고 할 때") {
            val saveStorePort = mockk<SaveStorePort>()
            val sellerPolicy = mockk<SellerPolicy>()
            val storeFactory = mockk<StoreFactory>()
            val publishEventPort = mockk<PublishEventPort>()

            val registerService =
                RegisterService(
                    sellerPolicy = sellerPolicy,
                    storeFactory = storeFactory,
                    saveStorePort = saveStorePort,
                    publishEventPort = publishEventPort,
                )

            val ownerUserId = UUID.randomUUID()
            val command =
                RegisterStoreCommand(
                    ownerUserId = ownerUserId,
                    name = "테크 스토어",
                    description = "최신 전자제품 판매",
                )

            every { sellerPolicy.checkOwnerRole(ownerUserId) } just runs
            every {
                storeFactory.createNewStore(
                    ownerUserId = ownerUserId,
                    name = command.name,
                    description = command.description,
                )
            } throws StoreException.DuplicateStore(ownerUserId)

            When("스토어를 등록하려고 하면") {
                Then("StoreException.DuplicateStore 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.DuplicateStore> {
                            registerService.register(command)
                        }

                    exception.message shouldBe "이미 스토어가 존재합니다. 판매자당 하나의 스토어만 운영할 수 있습니다"

                    // 예외 발생으로 충분히 검증하므로 verify 불필요
                }
            }
        }
    })
