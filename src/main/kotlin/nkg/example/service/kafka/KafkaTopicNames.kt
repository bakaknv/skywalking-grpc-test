package nkg.example.service.kafka

import org.apache.kafka.common.TopicPartition

/**
 * @author nkalugin on 4/21/20.
 */
const val KVTopic = "kv-topic"
val KVTopicPartition = TopicPartition(KVTopic, 0)