package ru.red.orderservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.orderservice.domain.Order;
import ru.red.orderservice.dto.OrderDTO;
import ru.red.orderservice.mapper.OrderMapper;
import ru.red.orderservice.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final OrderMapper mapper;

    @Autowired
    public OrderServiceImpl(OrderRepository repository,
                            OrderMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Order> createOrder(OrderDTO dto) {
        return repository
                .save(mapper.orderDTOToOrder(dto))
                .switchIfEmpty(Mono.error(new UnknownError("Failed creating notification")));
    }

    @Override
    public Flux<Order> fetchOrdersByEmail(String address) {
        return repository.findAllByUserAddress(address);
    }

    @Override
    public Flux<Order> fetchOrdersByTotalPriceBetween(Integer totalPriceStart, Integer totalPriceEnd) {
        // Expand prices to include previous and start arguments
        totalPriceStart -= 1;
        totalPriceEnd += 1;
        return repository.findAllByTotalPriceBetween(totalPriceStart, totalPriceEnd);
    }

    @Override
    public Mono<Order> fetchOrderById(String id) {
        return repository.findById(id);
    }
}
