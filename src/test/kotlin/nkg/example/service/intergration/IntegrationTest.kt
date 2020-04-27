package nkg.example.service.intergration

import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import nkg.example.service.Get
import nkg.example.service.KVServiceGrpc
import nkg.example.service.SpringBootApplicationMain
import nkg.example.service.service.GRpcServerProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers


/**
 * @author nkalugin on 4/25/20.
 */
@Testcontainers
@SpringBootTest(classes = [SpringBootApplicationMain::class])
@ContextConfiguration(initializers = [Initializer::class])
class IntegrationTest {
    companion object {
        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            kafka.start()
            aerospike.start()
        }
    }

    @Autowired
    lateinit var gRpcServerProperties: GRpcServerProperties

    @Test
    internal fun smokeTest() {
        val client = client()
        var getResponse = client.get(request("a", "b", "c"))
        assertEquals(0, getResponse.valuesCount, "service is empty")
        var putResponse = client.put(nkg.example.service.request(mapOf("a" to "a", "b" to "b")))
        assertTrue(putResponse.success, "put is succeed")
        getResponse = client.get(request("a", "b", "c"))
        assertEquals(2, getResponse.valuesCount, "two values found")
        putResponse = client.put(nkg.example.service.request(mapOf("b" to "c", "c" to "c")))
        assertTrue(putResponse.success, "new values were added")
        getResponse = client.get(request("b", "c"))
        assertTrue(getResponse.valuesList.all { it.value == "c" }, "b was updated to c")
    }

    fun client(): KVServiceGrpc.KVServiceBlockingStub {
        return KVServiceGrpc.newBlockingStub(managedChannel(gRpcServerProperties.port))
    }
}

fun managedChannel(port: Int): ManagedChannel? {
    val channel = ManagedChannelBuilder.forAddress("localhost", port)
        .usePlaintext()
        .build()
    while (channel.getState(true) != ConnectivityState.READY) Thread.sleep(100)
    return channel
}

fun request(vararg keys: String): Get.Request = request(keys.toList())

fun request(keys: List<String>): Get.Request = Get.Request.newBuilder().addAllKeys(keys).build()
