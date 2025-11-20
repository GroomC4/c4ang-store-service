package contracts.store

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return store by id"

    request {
        method GET()
        url "/api/v1/stores/1"
        headers {
            contentType(applicationJson())
        }
    }

    response {
        status 200
        body([
            id: 1,
            name: "Test Store",
            description: "A test store for contract testing"
        ])
        headers {
            contentType(applicationJson())
        }
    }
}