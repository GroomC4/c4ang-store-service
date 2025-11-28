package com.groom.store.adapter.out.event

import com.groom.store.adapter.out.event.dto.StoreInfoUpdatedEventDto
import com.groom.store.configuration.kafka.KafkaProperties
import com.groom.store.domain.event.StoreCreatedEvent
import com.groom.store.domain.event.StoreDeletedEvent
import com.groom.store.domain.event.StoreInfoUpdatedEvent
import com.groom.store.domain.port.PublishEventPort
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

        // TODO: StoreCreated 이벤트 구현
        // 현재는 로깅만 수행
    }

    override fun publishStoreInfoUpdated(event: StoreInfoUpdatedEvent) {
        val topic = kafkaProperties.topics.storeInfoUpdated
        val partitionKey = event.storeId.toString()

        // 변경된 필드 목록 생성
        val updatedFields = mutableListOf<String>()
        if (event.isNameChanged()) updatedFields.add("storeName")
        if (event.isDescriptionChanged()) updatedFields.add("storeDescription")
        if (event.isStatusChanged()) updatedFields.add("storeStatus")

        // Domain Event → Kafka Event DTO 변환
        val kafkaEvent =
            StoreInfoUpdatedEventDto(
                eventId = UUID.randomUUID().toString(),
                eventTimestamp = System.currentTimeMillis(),
                storeId = event.storeId.toString(),
                storeName = event.after.name,
                storeStatus = event.after.status.name,
                storeDescription = event.after.description,
                storePhone = null,
                storeAddress = null,
                businessHours = null,
                storeImageUrl = null,
                updatedFields = updatedFields,
                updatedAt = System.currentTimeMillis(),
            )

        logger.info {
            "Publishing store.info.updated event: eventId=${kafkaEvent.eventId}, " +
                "storeId=${event.storeId}, updatedFields=$updatedFields"
        }

        kafkaTemplate.send(topic, partitionKey, kafkaEvent).apply {
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
                            "eventId=${kafkaEvent.eventId}, storeId=${event.storeId}"
                    }
                }
            }
        }
    }

    override fun publishStoreDeleted(event: StoreDeletedEvent) {
        logger.info { "Publishing store.deleted event: storeId=${event.storeId}" }

        // TODO: StoreDeleted 이벤트 구현
        // 현재는 로깅만 수행
    }
}
