package com.groom.store.common

import com.groom.store.StoreApiApplication
import io.restassured.module.mockmvc.RestAssuredMockMvc
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest(classes = [StoreApiApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ContractTestBase {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        RestAssuredMockMvc.mockMvc(mockMvc)
    }
}