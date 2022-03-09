package ru.red.orderservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.red.orderservice.containers.ConfluentKafkaContainer;
import ru.red.orderservice.containers.MongoContainer;
import ru.red.orderservice.domain.Order;
import ru.red.orderservice.dto.OrderDTO;
import ru.red.orderservice.mapper.OrderMapper;
import ru.red.orderservice.util.OrderUtil;

@Testcontainers
@SpringBootTest
@ActiveProfiles({"test", "kafka"})
class OrderServiceImplTest {
    @Container
    public final static ConfluentKafkaContainer kafkaContainer = ConfluentKafkaContainer.getInstance();

    @Container
    public final static MongoDBContainer mongoDBContainer = MongoContainer.getInstance();

    @Autowired
    OrderService service;

    @Autowired
    OrderMapper mapper;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("kafka.bootstrapAddress", kafkaContainer::getBootstrapServers);
    }

    @Test
    void createOrder() {
        var order = OrderUtil.createRandom();
        var dto = mapper.orderToOrderDTO(order);
        StepVerifier.create(service.createOrder(dto))
                .expectNextMatches(o -> o.getUserAddress().equals(order.getUserAddress()) &&
                        o.getTotalPrice().equals(order.getTotalPrice()))
                .expectComplete()
                .verify();
    }

    private Flux<Order> createOrders(int count) {
        return Flux.generate(sink -> sink.next(mapper.orderToOrderDTO(OrderUtil.createRandom())))
                .take(count)
                .cast(OrderDTO.class)
                .flatMap(service::createOrder);
    }

    private Flux<Order> createOrders(int count, String address) {
        return Flux.generate(sink -> sink.next(
                                mapper.orderToOrderDTO(
                                        OrderUtil.createRandomWithAddress(address)
                                )
                        )
                )
                .take(count)
                .cast(OrderDTO.class)
                .flatMap(service::createOrder);
    }

    @Test
    void fetchNotificationsByAddress() {
        var count = 10;
        var email = "test@example.org"; // Using .org since util uses .com
        StepVerifier.create(createOrders(count))
                .expectNextCount(count)
                .expectComplete()
                .verify();

        var orders = createOrders(count / 2, email).cache(count);
        StepVerifier.create(orders)
                .expectNextCount(count / 2)
                .expectComplete()
                .verify();

        StepVerifier.create(service.fetchOrdersByEmail(email))
                .expectNextMatches(n -> Boolean.TRUE.equals(
                        orders.any(notification -> notification.equals(n)).block()))
                .expectNextCount(count / 2 - 1)
                .expectComplete()
                .verify();
    }

    @Test
    void fetchOrderTotalPrice() {
        var count = 10;
        StepVerifier.create(createOrders(count))
                .expectNextCount(count)
                .expectComplete()
                .verify();

        var order = OrderUtil.createRandom();
        order = service.createOrder(mapper.orderToOrderDTO(order)).block();

        assert order != null;

        var finalOrder = order;
        StepVerifier.create(service.fetchOrdersByTotalPriceBetween(order.getTotalPrice(), order.getTotalPrice()))
                .expectNextMatches(n -> n.equals(finalOrder))
                .expectComplete()
                .verify();
    }

    @Test
    void fetchNotificationById() {
        var order = OrderUtil.createRandom();
        order = service.createOrder(mapper.orderToOrderDTO(order)).block();
        assert order != null;
        var found = service.fetchOrderById(order.getId()).block();
        assert found != null;
        assert found.equals(order);
    }
}