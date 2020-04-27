package nkg.example.service.extentions

import nkg.example.service.kafka.KVTopicPartition
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition

/**
 * @author nkalugin on 4/23/20.
 */

fun <K, V> KafkaConsumer<K, V>.resetToCommittedOffset(topicPartition: TopicPartition): Long {
    val offsetAndMetadata = this.committed(setOf(topicPartition))[topicPartition]
    if (offsetAndMetadata == null) {
        seekToBeginning(setOf(topicPartition))
    } else {
        seek(KVTopicPartition, offsetAndMetadata.offset())
    }
    return position(KVTopicPartition)
}