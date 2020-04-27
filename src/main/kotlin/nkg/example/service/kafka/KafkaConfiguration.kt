package nkg.example.service.kafka

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author nkalugin on 4/21/20.
 */
@Configuration
class KafkaConfiguration {
    @Bean
    @ConfigurationProperties("kafka")
    fun kafkaProperties() = KafkaProperties()

    @Bean
    fun kafkaClientFactory(): ClientFactory = ClientFactoryImpl(kafkaProperties())
}


