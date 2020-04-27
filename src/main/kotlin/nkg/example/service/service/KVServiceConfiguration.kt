package nkg.example.service.service

import nkg.example.service.dao.KVStoreConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author nkalugin on 4/21/20.
 */
@Configuration
class KVServiceConfiguration(
    private val kvStoreConfiguration: KVStoreConfiguration
) {
    @Bean
    fun kvServiceThreadFactory(): CustomizableThreadFactory = CustomizableThreadFactory("kvService")

    @Bean
    @ConfigurationProperties("kv-service")
    fun kvServiceProperties(): KVServiceProperties =
        KVServiceProperties()

    @Bean
    fun kvServiceThreadPool(): ExecutorService =
        Executors.newFixedThreadPool(kvServiceProperties().threadCount, kvServiceThreadFactory())

    @Bean
    fun kvService() =
        KVServiceImpl(
            kvServiceThreadPool(),
            kvStoreConfiguration.kvStore()
        )

    @Bean
    fun kvServiceInitializer() =
        KVServiceInitializer(kvStoreConfiguration.kvStoreKafkaReader(), grpcServer())

    @Bean
    fun grpcThreadFactory() = CustomizableThreadFactory("grpcServer")

    @Bean
    @ConfigurationProperties("grpc")
    fun grpcProperties() = GRpcServerProperties()

    @Bean
    fun grpcExecutor(): ExecutorService =
        Executors.newFixedThreadPool(grpcProperties().grpcThreadCount, grpcThreadFactory())

    @Bean
    fun grpcServer() =
        GRpcServer(listOf(kvService()), grpcProperties(), grpcExecutor())
}

class GRpcServerProperties(var port: Int = 9999, var grpcThreadCount: Int = 4)

class KVServiceProperties(var threadCount: Int = 4)