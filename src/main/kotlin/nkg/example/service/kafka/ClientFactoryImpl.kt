package nkg.example.service.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer


class ClientFactoryImpl(private val kafkaProperties: KafkaProperties) : ClientFactory {
    override fun <K, V> createConsumer(
        clientId: String,
        key: Deserializer<K>,
        value: Deserializer<V>,
        properties: (Map<String, Any>) -> Unit
    ): KafkaConsumer<K, V> {
        val config = mapOf<String, Any>(
            ConsumerConfig.CLIENT_ID_CONFIG to clientId,
            ConsumerConfig.GROUP_ID_CONFIG to clientId,
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers
        )

        properties.invoke(config)
        return KafkaConsumer(config, key, value)
    }

    override fun <K, V> createProducer(
        clientId: String,
        key: Serializer<K>,
        value: Serializer<V>,
        properties: (Map<String, Any>) -> Unit
    ): KafkaProducer<K, V> {
        val config = mapOf<String, Any>(
            ProducerConfig.CLIENT_ID_CONFIG to clientId,
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers
        )

        properties.invoke(config)
        return KafkaProducer(config, key, value)
    }
}

class KafkaProperties(var bootstrapServers: String = "localhost:9092")
