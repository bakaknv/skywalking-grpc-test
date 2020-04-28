package nkg.example.service.dao

import com.aerospike.client.AerospikeClient
import com.aerospike.client.async.EventLoops
import com.aerospike.client.async.NettyEventLoops
import com.aerospike.client.policy.ClientPolicy
import com.github.aalobaidi.aerospike.reactor.AerospikeReactorClient
import io.netty.channel.nio.NioEventLoopGroup

/**
 * @author nkalugin on 4/28/20.
 */
interface AerospikeClientFactory {
    fun createClient(): AerospikeReactorClient

    fun eventLoops(): EventLoops
}

class AerospikeClientFactoryImpl(private val properties: AerospikeProperties) : AerospikeClientFactory {
    private val eventLoops = NettyEventLoops(NioEventLoopGroup(Runtime.getRuntime().availableProcessors()))

    override fun createClient(): AerospikeReactorClient {
        val policy = ClientPolicy()
        policy.eventLoops = eventLoops
        properties.clusterName?.let { policy.clusterName = it }
        properties.username?.let { policy.user = it }
        properties.password?.let { policy.password = it }
        return AerospikeReactorClient(AerospikeClient(policy, properties.host, properties.port))
    }

    override fun eventLoops() = eventLoops
}
