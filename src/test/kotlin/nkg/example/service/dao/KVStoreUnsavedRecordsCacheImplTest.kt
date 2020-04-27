package nkg.example.service.dao

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import nkg.example.service.request
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author nkalugin on 4/22/20.
 */
class KVStoreUnsavedRecordsCacheImplTest {
    @Test
    internal fun testLifecycle() {
        val persister = mock<KafkaPersister>() {
            onBlocking { persist(any()) } doReturnConsecutively listOf(1L, 2, 3)
        }
        val cache = KVStoreTransferringBufferImpl(KVStoreTransferringBufferProperties(1), persister)
        val slotId = runBlocking {
            cache.acquireSlotAndPut(request(mapOf("q" to "p")))
        }
        assertNotNull(slotId, "cache must has first permit")
        assertEquals(1, slotId)

        val errorSlotId = runBlocking {
            cache.acquireSlotAndPut(request(mapOf("o" to "p")))
        }

        assertNull(errorSlotId, "cache hasn't second permit")
        assertEquals("p", cache.get("q"), "cache must contains first value")
        assertNull(cache.get("o"), "cache hasn't second value")

        cache.releaseSlot(slotId!!)

        assertNull(cache.get("o"), "cache hasn't first value")

        val newSlotId = runBlocking {
            cache.acquireSlotAndPut(request(mapOf("e" to "d")))
        }

        assertEquals(newSlotId, 2, "cache has new slot after release")
    }


    @Test
    fun testRollbackOnPersisterError() {
        val errorRequest = request(mapOf("q" to "p"))
        val normalRequest = request(mapOf("a" to "b"))
        val persister = mock<KafkaPersister>() {
            onBlocking { persist(eq(errorRequest)) } doThrow RuntimeException("foo")
            onBlocking { persist(eq(normalRequest)) } doReturn 1
        }
        val cache = KVStoreTransferringBufferImpl(KVStoreTransferringBufferProperties(1), persister)

        assertThrows(java.lang.RuntimeException::class.java) {
            runBlocking {
                cache.acquireSlotAndPut(errorRequest)
            }
        }

        val slotId = runBlocking {
            cache.acquireSlotAndPut(normalRequest)
        }

        assertEquals(1, slotId, "cache has one permit")
    }
}