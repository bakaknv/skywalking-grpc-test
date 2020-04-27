package nkg.example.service.dao

import nkg.example.service.Entry
import nkg.example.service.Get
import nkg.example.service.Put

interface KVStore {
    suspend fun put(pairs: Put.Request): Put.Response
    suspend fun get(request: Get.Request): Get.Response
}

class KVStoreImpl(
    private val kvStoreTransferringBuffer: KVStoreTransferringBuffer,
    private val kvCachedStorageAccessor: KVCachedStorageAccessor
) : KVStore {
    override suspend fun put(pairs: Put.Request): Put.Response {
        try {
            kvStoreTransferringBuffer.acquireSlotAndPut(pairs) ?: return error("try again later")
            return success()
        } catch (e: Exception) {
            return error(e.message ?: "unknown error")
        }
    }

    override suspend fun get(request: Get.Request): Get.Response {
        val result = getFromTwoSources(request.keysList,
            { kvStoreTransferringBuffer.get(it) }) { kvCachedStorageAccessor.get(it) }

        val entries = result.entries.map { entry(it.key, it.value) }
        return Get.Response.newBuilder().addAllValues(entries).build()
    }

    private fun error(error: String) = Put.Response.newBuilder()
        .setSuccess(false)
        .setError(error)
        .build()

    private fun success() = Put.Response.newBuilder().setSuccess(true).build()

    private fun entry(key: String, value: String) = Entry.newBuilder().setKey(key).setValue(value).build()
}
