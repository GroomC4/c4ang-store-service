package com.groom.store.domain.service

import com.groom.ecommerce.common.annotation.UnitTest
import com.groom.ecommerce.common.exception.StoreException
import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.service.StoreFactory
import com.groom.store.domain.service.StorePolicy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.math.BigDecimal
import java.util.UUID

@UnitTest
class StoreFactoryTest :
    DescribeSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        describe("createNewStore") {
            context("유효한 스토어 정보가 주어졌을 때") {
                val storePolicy = mockk<StorePolicy>()
                val storeFactory = StoreFactory(storePolicy)
                val ownerExternalId = UUID.randomUUID()
                val storeName = "테크 스토어"
                val storeDescription = "최신 전자제품 판매"

                every { storePolicy.checkStoreAlreadyExists(ownerExternalId) } just runs

                val store =
                    storeFactory.createNewStore(
                        ownerUserId = ownerExternalId,
                        name = storeName,
                        description = storeDescription,
                    )

                it("Store 엔티티가 정상적으로 생성된다") {
                    store shouldNotBe null
                    store.ownerUserId shouldBe ownerExternalId
                    store.name shouldBe storeName
                    store.description shouldBe storeDescription
                    store.status shouldBe StoreStatus.REGISTERED
                    store.hiddenAt shouldBe null
                    store.deletedAt shouldBe null
                }

                it("StoreRating이 함께 생성되고 양방향 관계가 설정된다") {
                    store.rating shouldNotBe null
                    store.rating!!.store shouldBe store
                    store.rating!!.averageRating shouldBe BigDecimal.ZERO
                    store.rating!!.reviewCount shouldBe 0
                    store.rating!!.launchedAt shouldNotBe null
                }

                it("도메인 정책 검증이 수행된다") {
                    verify(exactly = 1) { storePolicy.checkStoreAlreadyExists(ownerExternalId) }
                }
            }

            context("스토어 설명이 없는 경우") {
                val storePolicy = mockk<StorePolicy>()
                val storeFactory = StoreFactory(storePolicy)
                val ownerExternalId = UUID.randomUUID()
                val storeName = "간단한 스토어"

                every { storePolicy.checkStoreAlreadyExists(ownerExternalId) } just runs

                val store =
                    storeFactory.createNewStore(
                        ownerUserId = ownerExternalId,
                        name = storeName,
                        description = null,
                    )

                it("description이 null로 설정된다") {
                    store shouldNotBe null
                    store.description shouldBe null
                    store.rating shouldNotBe null
                }
            }

            context("이미 스토어를 보유한 사용자인 경우") {
                val storePolicy = mockk<StorePolicy>()
                val storeFactory = StoreFactory(storePolicy)
                val ownerExternalId = UUID.randomUUID()

                every { storePolicy.checkStoreAlreadyExists(ownerExternalId) } throws
                    StoreException.DuplicateStore(ownerExternalId)

                it("StoreException.DuplicateStore 예외가 발생한다") {
                    shouldThrow<StoreException.DuplicateStore> {
                        storeFactory.createNewStore(
                            ownerUserId = ownerExternalId,
                            name = "테크 스토어",
                            description = "설명",
                        )
                    }
                }
            }

            context("여러 개의 스토어를 생성할 때") {
                val storePolicy = mockk<StorePolicy>()
                val storeFactory = StoreFactory(storePolicy)

                every { storePolicy.checkStoreAlreadyExists(any()) } just runs

                val stores =
                    (1..3).map { index ->
                        storeFactory.createNewStore(
                            ownerUserId = UUID.randomUUID(),
                            name = "스토어 $index",
                            description = "설명 $index",
                        )
                    }

                it("각 스토어가 독립적으로 생성된다") {
                    stores.size shouldBe 3
                    stores.forEach { store ->
                        store shouldNotBe null
                        store.rating shouldNotBe null
                    }

                    // 각 스토어의 ownerUserExternalId가 서로 다름
                    val ownerIds = stores.map { it.ownerUserId }.toSet()
                    ownerIds.size shouldBe 3
                }
            }
        }
    })
