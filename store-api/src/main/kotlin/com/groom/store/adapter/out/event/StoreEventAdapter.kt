package com.groom.store.adapter.out.event

import com.groom.store.configuration.kafka.KafkaProperties
import com.groom.store.domain.event.StoreCreatedEvent
import com.groom.store.domain.event.StoreDeletedEvent
import com.groom.store.domain.event.StoreInfoUpdatedEvent
import com.groom.store.domain.port.PublishEventPort
import com.groom.ecommerce.store.event.avro.StoreInfoUpdated
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * 스토어 이벤트 발행 Adapter.
 * Domain Port를 구현하고, Kafka를 통해 이벤트를 발행합니다.
 */
@Component
class StoreEventAdapter(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val kafkaProperties: KafkaProperties,
) : PublishEventPort {
    override fun publishStoreCreated(event: StoreCreatedEvent) {
        logger.info { "Publishing store.created event: storeId=${event.storeId}" }

        // TODO: StoreCreated Avro 스키마 추가 후 구현
        // 현재는 로깅만 수행
    }

    override fun publishStoreInfoUpdated(event: StoreInfoUpdatedEvent) {
        val topic = kafkaProperties.topics.storeInfoUpdated
        val partitionKey = event.storeId.toString()

        // Domain Event → Avro Event 변환
        val avroEvent =
            StoreInfoUpdated
                .newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventTimestamp(System.currentTimeMillis())
                .setStoreId(event.storeId.toString())
                .setStoreName(event.newName)
                .setStoreStatus("ACTIVE") // TODO: 실제 status 전달
                .setStoreDescription(event.newDescription)
                .setStorePhone(null)
                .setStoreAddress(null)
                .setBusinessHours(null)
                .setStoreImageUrl(null)
                .setUpdatedFields(listOf("storeName", "storeDescription"))
                .setUpdatedAt(System.currentTimeMillis())
                .build()

        logger.info { "Publishing store.info.updated event: eventId=${avroEvent.eventId}, storeId=${event.storeId}" }

        kafkaTemplate.send(topic, partitionKey, avroEvent).apply {
            whenComplete { result, ex ->
                if (ex == null) {
                    logger.info {
                        "Successfully published store.info.updated event: " +
                            "topic=${result.recordMetadata.topic()}, " +
                            "partition=${result.recordMetadata.partition()}, " +
                            "offset=${result.recordMetadata.offset()}"
                    }
                } else {
                    logger.error(ex) {
                        "Failed to publish store.info.updated event: " +
                            "eventId=${avroEvent.eventId}, storeId=${event.storeId}"
                    }
                }
            }
        }
    }

    override fun publishStoreDeleted(event: StoreDeletedEvent) {
        logger.info { "Publishing store.deleted event: storeId=${event.storeId}" }

        // TODO: StoreDeleted Avro 스키마 추가 후 구현
        // 현재는 로깅만 수행
    }
}
