package nkg.example.service.dao

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import nkg.example.service.Put
import nkg.example.service.extentions.resetToCommittedOffset
import nkg.example.service.kafka.ClientFactory
import nkg.example.service.kafka.KVTopicPartition
import nkg.example.service.kafka.MessageDeserializer
import nkg.example.service.kafka.assign
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.VoidDeserializer
import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import java.time.Duration
import java.util.concurrent.Executors

/**
 * @author nkalugin on 4/23/20.
 */
class KVStoreKafkaReader(
    clientFactory: ClientFactory,
    private val kvStoreTransferringBuffer: KVStoreTransferringBuffer,
    private val kvCachedStoragePersister: KVCachedStoragePersister
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val executor = Executors.newSingleThreadExecutor(CustomizableThreadFactory("kv-kafka-reader"))
        .asCoroutineDispatcher()

    private val consumer: KafkaConsumer<Void, Put.Request> = clientFactory.createConsumer(
        "kv-store-consumer",
        VoidDeserializer(),
        MessageDeserializer(Put.Request.getDefaultInstance())
    ).assign(KVTopicPartition)

    fun start() {
        recover()
        startLogProcessing()
    }

    private fun recover() {
        val lastOffset = consumer.endOffsets(listOf(KVTopicPartition))[KVTopicPartition] ?: return
        var currentOffset = consumer.position(KVTopicPartition)
        while (currentOffset < lastOffset) {
            val records = consumer.poll(Duration.ZERO).records(KVTopicPartition)
            records.forEach { kvStoreTransferringBuffer.recoverWith(it) }
            currentOffset = consumer.position(KVTopicPartition)
        }
    }

    private fun startLogProcessing() {
        logger.info("Reset to committed offset {}", consumer.resetToCommittedOffset(KVTopicPartition))
        GlobalScope.launch(executor) {
            var needReset = false
            while (true) {
                try {
                    if (needReset) {
                        val resetOffset = consumer.resetToCommittedOffset(KVTopicPartition)
                        logger.warn("Reset to committed offset due to previous errors {}", resetOffset)
                        needReset = false
                    }

                    val records = consumer.poll(Duration.ofSeconds(1)).records(KVTopicPartition)
                    if (records.isEmpty()) continue
                    val squashedRecords = squash(records)
                    val processedSlots = records.map { it.offset() }
                    kvCachedStoragePersister.put(squashedRecords, processedSlots)
                    consumer.commitSync()
                    logger.info("{} records was processed", records.size)
                } catch (e: Exception) {
                    logger.warn("Exception while processing records", e)
                    needReset = true
                }
            }
        }
    }

    private fun squash(records: List<ConsumerRecord<Void, Put.Request>>): Map<String, String> =
        records.map { it.value() }
            .flatMap { it.pairsList }
            .map { it.key to it.value }
            .toMap()
}

