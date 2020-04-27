package nkg.example.service.service

import com.google.protobuf.Message
import com.google.protobuf.TextFormat
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import nkg.example.service.Get
import nkg.example.service.KVServiceGrpc
import nkg.example.service.Put
import nkg.example.service.dao.KVStore
import nkg.example.service.extentions.sendResponse
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService


/**
 * @author nkalugin on 4/20/20.
 */
class KVServiceImpl(
    executorService: ExecutorService,
    private val kvStore: KVStore
) : KVServiceGrpc.KVServiceImplBase() {
    private val dispatcher = executorService.asCoroutineDispatcher()

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun put(request: Put.Request, responseObserver: StreamObserver<Put.Response>) {
        GlobalScope.launch(dispatcher) {
            responseObserver.sendResponse({
                kvStore.put(request)
            }, {
                onError(request, it)
            })
        }
    }

    override fun get(request: Get.Request, responseObserver: StreamObserver<Get.Response>) {
        GlobalScope.launch(dispatcher) {
            responseObserver.sendResponse({
                kvStore.get(request)
            }, {
                onError(request, it)
            })
        }
    }

    private fun onError(request: Message, e: Exception) {
        logger.warn(
            "Error while processing request {}",
            TextFormat.shortDebugString(request),
            e
        )
    }
}