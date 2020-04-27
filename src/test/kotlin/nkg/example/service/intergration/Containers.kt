package nkg.example.service.intergration

import nkg.example.service.dao.KVNamespace
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container

/**
 * @author nkalugin on 4/26/20.
 */
@Container
val kafka = KafkaContainer()

@Container
val aerospike = FixedHostPortGenericContainer<Nothing>("aerospike/aerospike-server").apply {
    waitingFor(AerospikeWaitStrategy())
    withFixedExposedPort(3000, 3000)
    withFixedExposedPort(3001, 3001)
    withFixedExposedPort(3002, 3002)
    withFixedExposedPort(3003, 3003)
    withEnv("NAMESPACE", KVNamespace)
}

class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(
            "kafka.bootstrap-servers=" + kafka.bootstrapServers
        ).applyTo(configurableApplicationContext.environment)
    }
}
