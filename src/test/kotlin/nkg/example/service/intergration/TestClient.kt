package nkg.example.service.intergration

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.protobuf.TextFormat
import nkg.example.service.Get
import nkg.example.service.KVServiceGrpc
import nkg.example.service.Put
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom

/**
 * @author nkalugin on 4/26/20.
 */
fun main() {
    val client = asyncClient()
    val executor = Executors.newSingleThreadExecutor(CustomizableThreadFactory("logger").apply { isDaemon = true })
    var counter = 1_000_000
    while (counter-- > 0) {
        Thread.sleep(1)
        Futures.addCallback(
            client.put(nkg.example.service.request(randomPut())),
            object : FutureCallback<Put.Response> {
                override fun onSuccess(response: Put.Response?) {
                    println(TextFormat.shortDebugString(response))
                }

                override fun onFailure(throwable: Throwable) {
                    println(throwable)
                }
            },
            executor
        )
        Futures.addCallback(
            client.get(request(randomGet())),
            object : FutureCallback<Get.Response> {
                override fun onSuccess(result: Get.Response?) {
                    println(TextFormat.shortDebugString(result))
                }

                override fun onFailure(throwable: Throwable) {
                    println(throwable)
                }
            },
            executor
        )
    }
}

private fun randomPut(): Map<String, String> =
    randomPairs().take(ThreadLocalRandom.current().nextInt(30)).toMap()

private fun randomPairs() = generateSequence { randomString() to randomString() }

private fun randomGet() = randomKeys().take(ThreadLocalRandom.current().nextInt(100)).toList()

private fun randomKeys() = generateSequence { randomString() }

private fun randomString() = ThreadLocalRandom.current().nextInt(10000).toString()

private fun asyncClient(): KVServiceGrpc.KVServiceFutureStub {
    return KVServiceGrpc.newFutureStub(managedChannel(9999))
}