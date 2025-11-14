package com.groom.store.infrastructure.event

import com.groom.ecommerce.store.event.avro.StoreInfoUpdated
import com.groom.store.configuration.kafka.KafkaProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger {}

/**
 * Store 도메인 이벤트 발행 담당
 *
 * Kafka를 통해 스토어 관련 이벤트를 비동기로 발행합니다.
 */
@Component
class StoreEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val kafkaProperties: KafkaProperties,
) {
    /**
     * 스토어 정보 변경 이벤트 발행
     *
     * @param event StoreInfoUpdated Avro 이벤트
     * @return CompletableFuture<SendResult<String, Any>>
     */
    fun publishStoreInfoUpdated(event: StoreInfoUpdated): CompletableFuture<SendResult<String, Any>> {
        val topic = kafkaProperties.topics.storeInfoUpdated
        val partitionKey = event.storeId.toString()

        logger.info { "Publishing store.info.updated event: eventId=${event.eventId}, storeId=${event.storeId}" }

        return kafkaTemplate.send(topic, partitionKey, event).apply {
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
                            "eventId=${event.eventId}, storeId=${event.storeId}"
                    }
                }
            }
        }
    }

    /**
     * 스토어 삭제 이벤트 발행 (기존 기능 유지를 위한 메서드)
     *
     * @param storeId 삭제된 스토어 ID
     */
    fun publishStoreDeleted(storeId: String) {
        val topic = kafkaProperties.topics.storeDeleted
        logger.info { "Publishing store.deleted event: storeId=$storeId" }

        // TODO: StoreDeleted Avro 이벤트 생성 및 발행 구현
        // 현재는 placeholder
    }
}
