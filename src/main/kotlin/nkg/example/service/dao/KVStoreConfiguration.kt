package nkg.example.service.dao

import io.micrometer.core.instrument.MeterRegistry
import nkg.example.service.kafka.KafkaConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author nkalugin on 4/21/20.
 */
@Configuration
class KVStoreConfiguration(
    private val kafkaConfiguration: KafkaConfiguration,
    private val aerospikeConfiguration: AerospikeConfiguration,
    private val meterRegistry: MeterRegistry
) {
    @Bean
    @ConfigurationProperties("kv-store.transferring-buffer")
    fun kvStoreTransferringBufferProperties() = KVStoreTransferringBufferProperties()

    @Bean
    fun kvKafkaPersister() = KafkaPersisterImpl(meterRegistry, kafkaConfiguration.kafkaClientFactory())

    @Bean
    fun kvStoreTransferringBuffer() =
        KVStoreTransferringBufferImpl(kvStoreTransferringBufferProperties(), kvKafkaPersister())

    @Bean
    @ConfigurationProperties("kv-store.evictable-cache")
    fun kvEvictableCacheProperties() = KVEvictableCacheProperties()


    @Bean
    fun kvCachedStorage() =
        KVCachedStorageImpl(
            kvEvictableCacheProperties(),
            aerospikeConfiguration.eventLoops(),
            aerospikeConfiguration.aerospikeClient(),
            kvStoreTransferringBuffer()
        )

    @Bean
    fun kvStore(): KVStore = KVStoreImpl(kvStoreTransferringBuffer(), kvCachedStorage())

    @Bean
    fun kvStoreKafkaReader() =
        KVStoreKafkaReader(kafkaConfiguration.kafkaClientFactory(), kvStoreTransferringBuffer(), kvCachedStorage())
}
