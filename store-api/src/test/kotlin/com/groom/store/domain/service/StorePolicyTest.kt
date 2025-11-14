package com.groom.store.domain.service

import com.groom.store.common.annotation.UnitTest
import com.groom.store.common.enums.StoreStatus
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.model.Store
import com.groom.store.domain.port.LoadStorePort
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID

@UnitTest
class StorePolicyTest :
    BehaviorSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        Given("사용자가 스토어를 보유하고 있지 않은 경우") {
            val loadStorePort = mockk<LoadStorePort>()
            val storePolicy = StorePolicy(loadStorePort)
            val ownerExternalId = UUID.randomUUID()

            every { loadStorePort.existsByOwnerUserId(ownerExternalId) } returns false

            When("checkStoreAlreadyExists를 호출하면") {
                Then("예외가 발생하지 않는다") {
                    shouldNotThrowAny {
                        storePolicy.checkStoreAlreadyExists(ownerExternalId)
                    }

                    verify(exactly = 1) { loadStorePort.existsByOwnerUserId(ownerExternalId) }
                }
            }
        }

        Given("사용자가 이미 스토어를 보유하고 있는 경우") {
            val loadStorePort = mockk<LoadStorePort>()
            val storePolicy = StorePolicy(loadStorePort)
            val ownerExternalId = UUID.randomUUID()

            every { loadStorePort.existsByOwnerUserId(ownerExternalId) } returns true

            When("checkStoreAlreadyExists를 호출하면") {
                Then("StoreException.DuplicateStore 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.DuplicateStore> {
                            storePolicy.checkStoreAlreadyExists(ownerExternalId)
                        }

                    exception.message shouldContain "이미 스토어가 존재합니다"
                    verify(exactly = 1) { loadStorePort.existsByOwnerUserId(ownerExternalId) }
                }
            }
        }

        Given("정상 상태의 스토어인 경우") {
            val loadStorePort = mockk<LoadStorePort>()
            val storePolicy = StorePolicy(loadStorePort)
            val storeId = UUID.randomUUID()
            val store =
                mockk<Store> {
                    every { status } returns StoreStatus.REGISTERED
                }

            every { loadStorePort.loadById(storeId) } returns store

            When("checkStoreDeletable을 호출하면") {
                Then("예외가 발생하지 않는다") {
                    shouldNotThrowAny {
                        storePolicy.checkStoreDeletable(storeId)
                    }

                    verify(exactly = 1) { loadStorePort.loadById(storeId) }
                }
            }
        }

        Given("이미 삭제된 스토어인 경우") {
            val loadStorePort = mockk<LoadStorePort>()
            val storePolicy = StorePolicy(loadStorePort)
            val storeId = UUID.randomUUID()
            val store =
                mockk<Store> {
                    every { status } returns StoreStatus.DELETED
                }

            every { loadStorePort.loadById(storeId) } returns store

            When("checkStoreDeletable을 호출하면") {
                Then("StoreException.StoreAlreadyDeleted 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.StoreAlreadyDeleted> {
                            storePolicy.checkStoreDeletable(storeId)
                        }

                    exception.message shouldContain "이미 삭제된 스토어입니다"
                    verify(exactly = 1) { loadStorePort.loadById(storeId) }
                }
            }
        }

        Given("존재하지 않는 스토어인 경우") {
            val loadStorePort = mockk<LoadStorePort>()
            val storePolicy = StorePolicy(loadStorePort)
            val storeId = UUID.randomUUID()

            every { loadStorePort.loadById(storeId) } returns null

            When("checkStoreDeletable을 호출하면") {
                Then("StoreException.StoreNotFound 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.StoreNotFound> {
                            storePolicy.checkStoreDeletable(storeId)
                        }

                    exception.message shouldContain "스토어를 찾을 수 없습니다"
                    verify(exactly = 1) { loadStorePort.loadById(storeId) }
                }
            }
        }

        Given("스토어 소유자가 접근하는 경우") {
            val loadStorePort = mockk<LoadStorePort>()
            val storePolicy = StorePolicy(loadStorePort)
            val storeId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val store =
                mockk<Store> {
                    every { ownerUserId } returns userId
                }

            every { loadStorePort.loadById(storeId) } returns store

            When("checkStoreAccess를 호출하면") {
                Then("예외가 발생하지 않는다") {
                    shouldNotThrowAny {
                        storePolicy.checkStoreAccess(storeId, userId)
                    }

                    verify(exactly = 1) { loadStorePort.loadById(storeId) }
                }
            }
        }

        Given("스토어 소유자가 아닌 사용자가 접근하는 경우") {
            val loadStorePort = mockk<LoadStorePort>()
            val storePolicy = StorePolicy(loadStorePort)
            val storeId = UUID.randomUUID()
            val ownerId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val store =
                mockk<Store> {
                    every { ownerUserId } returns ownerId
                }

            every { loadStorePort.loadById(storeId) } returns store

            When("checkStoreAccess를 호출하면") {
                Then("StoreException.StoreAccessDenied 예외가 발생한다") {
                    val exception =
                        shouldThrow<StoreException.StoreAccessDenied> {
                            storePolicy.checkStoreAccess(storeId, userId)
                        }

                    exception.message shouldContain "스토어에 대한 접근 권한이 없습니다"
                    verify(exactly = 1) { loadStorePort.loadById(storeId) }
                }
            }
        }
    })
