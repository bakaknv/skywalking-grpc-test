package nkg.example.service.service

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService

/**
 * @author nkalugin on 4/21/20.
 */
class GRpcServer(
    private val services: Collection<BindableService>,
    private val properties: GRpcServerProperties,
    private val executor: ExecutorService
) {
    private var logger = LoggerFactory.getLogger(javaClass)

    private var server: Server? = null

    fun start() {
        val builder = ServerBuilder.forPort(properties.port)
        services.forEach { builder.addService(it) }
        builder.executor(executor)
        val build = builder.build()
        build.start()
        server = build
        logger.info("GRpc server started with {} services", build.services.size)
        // let me unsee this
        startDaemonAwaitThread(build)
    }

    private fun startDaemonAwaitThread(server: Server) {
        val awaitThread = Thread {
            try {
                server.awaitTermination()
            } catch (e: InterruptedException) {
                logger.error("GRpc server stopped.", e)
            }
        }
        awaitThread.isDaemon = false
        awaitThread.start()
    }

    fun stop() {
        server?.shutdown()
    }
}