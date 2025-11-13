package com.groom.store

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
@EnableRetry
@EnableFeignClients
class StoreServiceApiApplication

fun main(args: Array<String>) {
    runApplication<StoreServiceApiApplication>(*args)
}
