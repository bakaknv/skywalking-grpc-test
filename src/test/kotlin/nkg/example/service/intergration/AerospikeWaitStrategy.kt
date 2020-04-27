package nkg.example.service.intergration

import com.aerospike.client.AerospikeClient
import com.aerospike.client.AerospikeException
import org.slf4j.LoggerFactory
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy


/**
 * @author nkalugin on 4/25/20.
 */
open class AerospikeWaitStrategy : AbstractWaitStrategy() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun waitUntilReady() {
        while (!isReady()) {
            Thread.sleep(100)
        }
    }

    private fun isReady(): Boolean {
        val containerId: String = waitStrategyTarget.containerId
        log.debug("Check Aerospike container {} status", containerId)
        val containerInfo = waitStrategyTarget.containerInfo
        if (containerInfo == null) {
            log.debug(
                "Aerospike container[{}] doesn't contain info. Abnormal situation, should not happen.",
                containerId
            )
            return false
        }
        val port = 3000
        val host = "localhost"

        try {
            AerospikeClient(host, port).use { client -> return client.isConnected }
        } catch (e: AerospikeException.Connection) {
            log.debug("Aerospike container: {} not yet started. {}", containerId, e.message)
        }
        return false
    }
}