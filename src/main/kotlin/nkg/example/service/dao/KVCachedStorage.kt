package nkg.example.service.dao

import com.aerospike.client.BatchRead
import com.aerospike.client.Bin
import com.aerospike.client.Key
import com.aerospike.client.async.EventLoops
import com.github.aalobaidi.aerospike.reactor.AerospikeReactorClient
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.reactive.awaitFirst
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author nkalugin on 4/22/20.
 */
interface KVCachedStorageAccessor {
    suspend fun get(keys: Collection<String>): Map<String, String>
}

interface KVCachedStoragePersister {
    suspend fun put(
        pairs: Map<String, String>,
        processedSlots: List<Long>
    )
}

const val KVNamespace = "kv-store"
private const val KVValueBin = "val-bin"

class KVCachedStorageImpl(
    cacheProperties: KVEvictableCacheProperties,
    private val eventLoops: EventLoops,
    private val aerospikeClient: AerospikeReactorClient,
    private val kvStoreTransferringBuffer: KVStoreTransferringBuffer
) : KVCachedStorageAccessor, KVCachedStoragePersister {

    private val cache = CacheBuilder.newBuilder()
        .expireAfterAccess(cacheProperties.ttlMs, TimeUnit.MILLISECONDS)
        .maximumSize(cacheProperties.maxSize)
        .build<String, String>()

    override suspend fun get(keys: Collection<String>): Map<String, String> {
        return getFromTwoSources(keys, { cache.getIfPresent(it) }) { getFromStorage(it) }
    }

    private suspend fun getFromStorage(notFoundKeys: Collection<String>): Map<String, String> {
        if (notFoundKeys.isNotEmpty()) {
            val batchReads = notFoundKeys.map { BatchRead(Key(KVNamespace, null, it), arrayOf(KVValueBin)) }
            val kvEntries = aerospikeClient.get(
                eventLoops.next(), null, batchReads
            ).awaitFirst()

            return kvEntries.filter { it.record != null }
                .map { it.key.userKey.toString() to it.record.bins[KVValueBin] as String }.toMap()
        }
        return Collections.emptyMap()
    }

    override suspend fun put(
        pairs: Map<String, String>,
        processedSlots: List<Long>
    ) {
        cache.putAll(pairs)
        val putResults = pairs.map {
            aerospikeClient.put(eventLoops.next(), null, Key(KVNamespace, null, it.key), Bin(KVValueBin, it.value))
        }
        putResults.map { it.awaitFirst() }
        processedSlots.forEach { slotId -> kvStoreTransferringBuffer.releaseSlot(slotId) }
    }
}


class KVEvictableCacheProperties(var maxSize: Long = 100000, var ttlMs: Long = 10000)