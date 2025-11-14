package com.groom.store.application.service

import com.groom.ecommerce.common.annotation.UnitTest
import com.groom.ecommerce.common.exception.StoreException
import com.groom.store.application.dto.GetStoreQuery
import com.groom.store.common.enums.StoreStatus
import com.groom.store.fixture.StoreTestFixture
import com.groom.store.outbound.repository.StoreRepositoryImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.util.Optional
import java.util.UUID

@UnitTest
class GetStoreServiceTest :
    BehaviorSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        Given("존재하는 스토어를 조회하려고 할 때") {
            val storeRepository = mockk<StoreRepositoryImpl>()

            val getStoreService =
                GetStoreService(
                    reader = storeRepository,
                )

            val storeId = UUID.randomUUID()
            val ownerId = UUID.randomUUID()
            val query = GetStoreQuery(storeId = storeId)

            val existingStore =
                StoreTestFixture.createStoreWithRating(
                    id = storeId,
                    ownerUserId = ownerId,
                    name = "테스트 스토어",
                    description = "테스트 설명",
                    status = StoreStatus.REGISTERED,
                    averageRating = java.math.BigDecimal("4.5"),
                    reviewCount = 100,
                )

            every { storeRepository.findById(storeId) } returns Optional.of(existingStore)

            When("스토어를 조회하면") {
                val result = getStoreService.getStore(query)

                Then("스토어 상세 정보가 반환된다") {
                    result shouldNotBe null
                    result.storeId shouldBe storeId.toString()
                    result.ownerUserId shouldBe ownerId.toString()
                    result.name shouldBe "테스트 스토어"
                    result.description shouldBe "테스트 설명"
                    result.status shouldBe StoreStatus.REGISTERED
                    result.averageRating shouldBe java.math.BigDecimal("4.5")
                    result.reviewCount shouldBe 100
                    result.launchedAt shouldNotBe null
                    result.createdAt shouldNotBe null
                    result.updatedAt shouldNotBe null
                }
            }
        }

        Given("존재하지 않는 스토어를 조회하려고 할 때") {
            val storeRepository = mockk<StoreRepositoryImpl>()

            val getStoreService =
                GetStoreService(
                    reader = storeRepository,
                )

            val storeId = UUID.randomUUID()
            val query = GetStoreQuery(storeId = storeId)

            every { storeRepository.findById(storeId) } returns Optional.empty()

            When("스토어를 조회하려고 하면") {
                Then("StoreException.StoreNotFound 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.StoreNotFound> {
                            getStoreService.getStore(query)
                        }

                    exception.storeId shouldBe storeId
                }
            }
        }

        Given("삭제된 스토어를 조회하려고 할 때") {
            val storeRepository = mockk<StoreRepositoryImpl>()

            val getStoreService =
                GetStoreService(
                    reader = storeRepository,
                )

            val storeId = UUID.randomUUID()
            val ownerId = UUID.randomUUID()
            val query = GetStoreQuery(storeId = storeId)

            val deletedStore =
                StoreTestFixture.createStoreWithRating(
                    id = storeId,
                    ownerUserId = ownerId,
                    name = "삭제된 스토어",
                    description = "삭제됨",
                    status = StoreStatus.DELETED,
                    averageRating = java.math.BigDecimal.ZERO,
                    reviewCount = 0,
                )

            every { storeRepository.findById(storeId) } returns Optional.of(deletedStore)

            When("스토어를 조회하면") {
                val result = getStoreService.getStore(query)

                Then("삭제된 스토어도 조회할 수 있다 (관리 목적)") {
                    result shouldNotBe null
                    result.storeId shouldBe storeId.toString()
                    result.status shouldBe StoreStatus.DELETED
                    result.name shouldBe "삭제된 스토어"
                }
            }
        }

        Given("평점 정보가 없는 스토어를 조회할 때") {
            val storeRepository = mockk<StoreRepositoryImpl>()

            val getStoreService =
                GetStoreService(
                    reader = storeRepository,
                )

            val storeId = UUID.randomUUID()
            val ownerId = UUID.randomUUID()
            val query = GetStoreQuery(storeId = storeId)

            val storeWithoutRating =
                StoreTestFixture.createStore(
                    id = storeId,
                    ownerUserId = ownerId,
                    name = "신규 스토어",
                    description = "평점 없음",
                    status = StoreStatus.REGISTERED,
                )

            every { storeRepository.findById(storeId) } returns Optional.of(storeWithoutRating)

            When("스토어를 조회하면") {
                val result = getStoreService.getStore(query)

                Then("평점 정보가 null로 반환된다") {
                    result shouldNotBe null
                    result.storeId shouldBe storeId.toString()
                    result.averageRating shouldBe null
                    result.reviewCount shouldBe 0
                    result.launchedAt shouldBe null
                }
            }
        }
    })
