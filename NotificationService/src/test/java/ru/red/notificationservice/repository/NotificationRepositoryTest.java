package ru.red.notificationservice.repository;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.red.notificationservice.containers.MongoContainer;
import ru.red.notificationservice.util.NotificationUtil;

import java.util.Objects;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class NotificationRepositoryTest {
    @Container
    public final static MongoDBContainer mongoDBContainer = MongoContainer.getInstance();

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    NotificationRepository repository;

    @Test
    void createNotification() {
        var notification = NotificationUtil.createRandom();
        StepVerifier.create(repository.save(notification))
                .expectNextMatches(n ->
                        Objects.requireNonNull(repository
                                        .findAllByUserAddress(notification.getUserAddress())
                                        .blockFirst())
                        .equals(n))
                .expectComplete()
                .log()
                .verify();
    }
}