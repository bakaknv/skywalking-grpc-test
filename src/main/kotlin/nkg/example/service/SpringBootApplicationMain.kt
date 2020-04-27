package nkg.example.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * @author nkalugin on 4/21/20.
 */
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
@EnableScheduling
class SpringBootApplicationMain

fun main(args: Array<String>) {
    runApplication<SpringBootApplicationMain>(*args)

}
