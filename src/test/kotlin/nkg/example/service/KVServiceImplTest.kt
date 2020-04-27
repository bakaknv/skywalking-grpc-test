package nkg.example.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import io.grpc.ManagedChannelBuilder
import io.grpc.Server
import io.grpc.ServerBuilder
import nkg.example.service.dao.KVStore
import nkg.example.service.service.KVServiceImpl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

/**
 * @author nkalugin on 4/20/20.
 */
internal class KVServiceImplTest {
    var server: Server? = null

    @AfterEach
    internal fun tearDown() {
        server?.shutdown()
    }

    @Test
    internal fun testPut() {
        val kvStore = mock<KVStore>() {
            onBlocking { put(any()) } doReturn Put.Response.newBuilder().setSuccess(true).build()
        }
        val stub = kvServiceBlockingStub(kvStore)
        val response = stub.put(request(mapOf("k" to "v")))
        Assertions.assertTrue(response.success, "Put is succeed")
    }

    @Test
    internal fun testFailedPut() {
        val kvStore = mock<KVStore>() {
            onBlocking { put(any()) } doReturn Put.Response.newBuilder().setSuccess(false).build()
        }
        val stub = kvServiceBlockingStub(kvStore)
        val response = stub.put(request(mapOf("k" to "v")))
        Assertions.assertFalse(response.success, "Put is failed")
    }


    @Test
    fun testError() {
        val kvStore = mock<KVStore>() {
            onBlocking { put(any()) } doThrow RuntimeException("foo")
        }
        val stub = kvServiceBlockingStub(kvStore)
        Assertions.assertThrows(RuntimeException::class.java) {
            stub.put(request(mapOf("k" to "v")))
        }
    }

    private fun kvServiceBlockingStub(kvStore: KVStore): KVServiceGrpc.KVServiceBlockingStub {
        val kvService =
            KVServiceImpl(Executors.newSingleThreadExecutor(), kvStore)
        server = ServerBuilder.forPort(0).addService(kvService).build()
        server!!.start()

        val channel = ManagedChannelBuilder.forAddress("localhost", server!!.port)
            .usePlaintext()
            .build()
        return KVServiceGrpc.newBlockingStub(channel)
    }
}

fun request(map: Map<String, String>): Put.Request =
    Put.Request.newBuilder().addAllPairs(map.map { (k, v) -> Entry.newBuilder().setKey(k).setValue(v).build() }).build()