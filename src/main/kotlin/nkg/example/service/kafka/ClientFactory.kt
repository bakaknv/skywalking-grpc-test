package nkg.example.service.kafka

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

interface ClientFactory {
    fun <K, V> createConsumer(
        clientId: String,
        key: Deserializer<K>,
        value: Deserializer<V>,
        properties: (Map<String, Any>) -> Unit
    ): KafkaConsumer<K, V>

    fun <K, V> createConsumer(clientId: String, key: Deserializer<K>, value: Deserializer<V>) =
        createConsumer(
            clientId,
            key,
            value,
            { }
        )

    fun <K, V> createProducer(
        clientId: String,
        key: Serializer<K>,
        value: Serializer<V>,
        properties: (Map<String, Any>) -> Unit
    ): KafkaProducer<K, V>

    fun <K, V> createProducer(
        clientId: String,
        key: Serializer<K>,
        value: Serializer<V>
    ): KafkaProducer<K, V> = createProducer(clientId, key, value, { })
}

fun <K, V> KafkaConsumer<K, V>.assign(topicPartition: TopicPartition): KafkaConsumer<K, V> {
    this.assign(listOf(topicPartition))
    return this
}
