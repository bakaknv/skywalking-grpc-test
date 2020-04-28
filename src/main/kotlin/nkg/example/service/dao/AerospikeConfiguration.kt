package nkg.example.service.dao

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
    fun aerospikeClientFactory() = AerospikeClientFactoryImpl(aerospikeProperties())
}

class AerospikeProperties(
    var host: String = "localhost",
    var port: Int = 3000,
    var clusterName: String? = null,
    var username: String? = null,
    var password: String? = null
)