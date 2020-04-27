package nkg.example.service.service

import nkg.example.service.dao.KVStoreKafkaReader
import org.springframework.context.SmartLifecycle

/**
 * @author nkalugin on 4/23/20.
 */
class KVServiceInitializer(
    private val kvStoreKafkaReader: KVStoreKafkaReader,
    private val gRpcServer: GRpcServer
) : SmartLifecycle {
    private var isRunning = false

    override fun isRunning(): Boolean = isRunning

    override fun start() {
        Thread {
            kvStoreKafkaReader.start()
            gRpcServer.start()
        }.start()
        isRunning = true
    }

    override fun stop() {
        gRpcServer.stop()
        isRunning = false
    }
}