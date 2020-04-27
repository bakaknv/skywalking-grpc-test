package nkg.example.service.dao

import com.aerospike.client.AerospikeClient
import com.aerospike.client.async.NettyEventLoops
import com.aerospike.client.policy.ClientPolicy
import com.github.aalobaidi.aerospike.reactor.AerospikeReactorClient
import io.netty.channel.nio.NioEventLoopGroup
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author nkalugin on 4/24/20.
 */
@Configuration
class AerospikeConfiguration {
    @Bean
    @ConfigurationProperties("aerospike")
    fun aerospikeProperties(): AerospikeProperties = AerospikeProperties()

    @Bean
    fun eventLoopGroup() = NioEventLoopGroup(Runtime.getRuntime().availableProcessors())

    @Bean
    fun eventLoops() = NettyEventLoops(eventLoopGroup())

    @Bean
    fun aerospikeClient(): AerospikeReactorClient {
        val policy = ClientPolicy()
        policy.eventLoops = eventLoops()
        val properties = aerospikeProperties()
        properties.clusterName?.let { policy.clusterName = it }
        properties.username?.let { policy.user = it }
        properties.password?.let { policy.password = it }
        return AerospikeReactorClient(AerospikeClient(policy, properties.host, properties.port))
    }
}

class AerospikeProperties(
    var host: String = "localhost",
    var port: Int = 3000,
    var clusterName: String? = null,
    var username: String? = null,
    var password: String? = null
)