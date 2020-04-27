package nkg.example.service.extentions

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @author nkalugin on 4/21/20.
 */
suspend inline fun <K, V> KafkaProducer<K, V>.sendAsync(record: ProducerRecord<K, V>) =
    suspendCoroutine<RecordMetadata> { continuation ->
        send(record) { metadata: RecordMetadata?, exception: Exception? ->
            if (metadata == null && exception != null) {
                continuation.resumeWithException(exception)
            } else if (metadata != null && exception == null) {
                continuation.resume(metadata)
            }
        }
    }
