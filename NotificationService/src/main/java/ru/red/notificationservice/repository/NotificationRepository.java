package ru.red.notificationservice.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import ru.red.notificationservice.domain.Notification;

public interface NotificationRepository extends ReactiveMongoRepository<Notification, ObjectId> {
    Flux<Notification> findAllByUserAddress(String userAddress);
    Flux<Notification> findAllByContentsContaining(String contents);
}
