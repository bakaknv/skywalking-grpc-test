package nkg.example.service.dao

import com.google.protobuf.Message
import io.micrometer.core.instrument.MeterRegistry
import nkg.example.service.Put
import nkg.example.service.extentions.sendAsync
import nkg.example.service.kafka.ClientFactory
import nkg.example.service.kafka.KVTopicPartition
import nkg.example.service.kafka.MessageSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.VoidSerializer

interface KafkaPersister {
    @Throws(Exception::class)
    suspend fun persist(request: Put.Request): Long
}

class KafkaPersisterImpl(meterRegistry: MeterRegistry, kafkaClientFactory: ClientFactory) : KafkaPersister {
    private val kafkaProducer: KafkaProducer<Void?, Message> =
        kafkaClientFactory.createProducer("kvProducer", VoidSerializer(), MessageSerializer())

    private val counter = meterRegistry.counter("kv-kafka-persisted")

    @Throws(Exception::class)
    override suspend fun persist(request: Put.Request): Long {
        val record: ProducerRecord<Void?, Message> =
            ProducerRecord(KVTopicPartition.topic(), KVTopicPartition.partition(), null, request)
        val metadata = kafkaProducer.sendAsync(record)
        counter.increment(request.pairsCount.toDouble())
        return metadata.offset()
    }
}