package nkg.example.service.kafka

import com.google.protobuf.Message
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

/**
 * @author nkalugin on 4/21/20.
 */
class MessageSerializer : Serializer<Message> {
    override fun serialize(topic: String?, data: Message?): ByteArray? {
        return data?.toByteArray()
    }
}

class MessageDeserializer<T : Message>(private val example: T) : Deserializer<T> {
    override fun deserialize(topic: String?, data: ByteArray?): T {
        @Suppress("UNCHECKED_CAST")
        return example.toBuilder().mergeFrom(data).build() as T
    }
}
