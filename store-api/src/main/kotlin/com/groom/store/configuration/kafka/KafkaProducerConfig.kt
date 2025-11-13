package com.groom.store.configuration.kafka

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

/**
 * Kafka Producer 설정
 *
 * Avro 직렬화를 사용하여 타입 안전한 이벤트 발행
 */
@Configuration
class KafkaProducerConfig(
    private val kafkaProperties: KafkaProperties,
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val configProps = mutableMapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to kafkaProperties.producer.acks,
            ProducerConfig.RETRIES_CONFIG to kafkaProperties.producer.retries,
            ProducerConfig.BATCH_SIZE_CONFIG to kafkaProperties.producer.batchSize,
            ProducerConfig.LINGER_MS_CONFIG to kafkaProperties.producer.lingerMs,
            ProducerConfig.BUFFER_MEMORY_CONFIG to kafkaProperties.producer.bufferMemory,
            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to kafkaProperties.producer.maxInFlightRequestsPerConnection,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to kafkaProperties.producer.enableIdempotence,
            ProducerConfig.COMPRESSION_TYPE_CONFIG to kafkaProperties.producer.compressionType,
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to kafkaProperties.schemaRegistry.url,
        )

        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory())
}

/**
 * Kafka 설정 Properties
 */
@Configuration
@ConfigurationProperties(prefix = "kafka")
data class KafkaProperties(
    var bootstrapServers: String = "localhost:9092",
    val producer: ProducerProperties = ProducerProperties(),
    val schemaRegistry: SchemaRegistryProperties = SchemaRegistryProperties(),
    val topics: TopicProperties = TopicProperties(),
)

data class ProducerProperties(
    var acks: String = "all",
    var retries: Int = 3,
    var batchSize: Int = 16384,
    var lingerMs: Int = 10,
    var bufferMemory: Long = 33554432L,
    var maxInFlightRequestsPerConnection: Int = 5,
    var enableIdempotence: Boolean = true,
    var compressionType: String = "snappy",
    var keySerializer: String = "org.apache.kafka.common.serialization.StringSerializer",
    var valueSerializer: String = "io.confluent.kafka.serializers.KafkaAvroSerializer",
)

data class SchemaRegistryProperties(
    var url: String = "http://localhost:8081",
)

data class TopicProperties(
    var storeInfoUpdated: String = "store.info.updated",
    var storeDeleted: String = "store.deleted",
)
