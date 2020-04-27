package nkg.example.service.dao

import com.google.protobuf.TextFormat
import nkg.example.service.Put
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

/**
 * @author nkalugin on 4/22/20.
 */
interface KVStoreTransferringBuffer {
    @Throws(Exception::class)
    suspend fun acquireSlotAndPut(request: Put.Request): Long?

    fun recoverWith(record: ConsumerRecord<Void, Put.Request>)

    fun get(key: String): String?

    fun releaseSlot(slotId: Long)
}

class KVStoreTransferringBufferImpl(
    properties: KVStoreTransferringBufferProperties,
    private val kafkaPersister: KafkaPersister
) : KVStoreTransferringBuffer {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val semaphore: Semaphore = Semaphore(properties.maxCacheSize)
    private val slotsToKeys = ConcurrentHashMap<Long, Collection<String>>()
    private val backingCache = ConcurrentHashMap<String, String>()

    @Throws(Exception::class)
    override suspend fun acquireSlotAndPut(request: Put.Request): Long? {
        if (semaphore.tryAcquire(request.pairsCount)) {
            val kafkaOffset = try {
                kafkaPersister.persist(request)
            } catch (e: Exception) {
                logger.warn("Error while persisting request {}", TextFormat.shortDebugString(request), e)
                semaphore.release(request.pairsCount)
                throw e
            }
            updateMaps(kafkaOffset, request)
            return kafkaOffset
        }
/*
        logger.warn(
            "Not enough permits for request {} current available permits {}",
            TextFormat.shortDebugString(request),
            semaphore.availablePermits()
        )
*/
        return null
    }

    private fun updateMaps(kafkaOffset: Long, request: Put.Request) {
        slotsToKeys[kafkaOffset] = request.pairsList.map { it.key }
        request.pairsList.forEach { backingCache[it.key] = it.value }
    }

    override fun recoverWith(record: ConsumerRecord<Void, Put.Request>) {
        semaphore.acquire(record.value().pairsCount)
        updateMaps(record.offset(), record.value())
    }

    override fun get(key: String): String? {
        return backingCache[key]
    }

    override fun releaseSlot(slotId: Long) {
        val keys = slotsToKeys[slotId]
        keys?.forEach { key -> backingCache.remove(key) }
        keys?.let { semaphore.release(it.size) }
    }
}

class KVStoreTransferringBufferProperties(var maxCacheSize: Int = 100000)
