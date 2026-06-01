package com.example.innowise_vitali.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = InnowiseVitaliApplicationTests.EmptyConfig.class,
        properties = {
                "grpc.server.enabled=false",
                "grpc.server.port=-1"
        }
)
@org.springframework.test.context.ActiveProfiles("test")
class InnowiseVitaliApplicationTests {

    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    static class EmptyConfig {
    }

    @Test
    void contextLoads() {
    }
}