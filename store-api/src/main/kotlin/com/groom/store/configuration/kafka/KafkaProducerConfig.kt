package com.groom.store.configuration.kafka

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

/**
 * Kafka Producer 설정
 *
 * Spring Boot의 KafkaProperties를 사용하여 Testcontainers 동적 포트 연동 지원
 * JSON 직렬화를 사용하여 이벤트 발행
 */
@Configuration
class KafkaProducerConfig(
    private val kafkaProperties: KafkaProperties,
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val configProps =
            mutableMapOf<String, Any>(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            )

        // Spring Boot KafkaProperties에서 설정된 producer 옵션 추가
        configProps.putAll(kafkaProperties.buildProducerProperties(null))

        // JSON Serializer 강제 적용 (buildProducerProperties가 덮어쓸 수 있으므로)
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java

        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory())
}

/**
 * Kafka Topic 설정 Properties
 */
@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
data class KafkaTopicProperties(
    var storeInfoUpdated: String = "store.info.updated",
    var storeDeleted: String = "store.deleted",
)
