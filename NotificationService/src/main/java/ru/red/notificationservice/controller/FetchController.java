package ru.red.notificationservice.controller;

import lombok.extern.log4j.Log4j2;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.notificationservice.controller.exceptions.NotFoundException;
import ru.red.notificationservice.domain.Notification;
import ru.red.notificationservice.service.NotificationService;

@Log4j2
@RestController
@RequestMapping("/api/notifications")
public class FetchController {
    private final NotificationService service;

    @Autowired
    public FetchController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/id")
    public Mono<Notification> fetchNotificationById(@RequestParam("id") ObjectId id) {
        return service.fetchNotificationById(id).as(this::validation);
    }

    @GetMapping("/email")
    public Flux<Notification> fetchNotificationsByAddress(@RequestParam("email") String address) {
        return service.fetchNotificationsByAddress(address).as(this::validation);
    }

    @GetMapping("/contents")
    public Flux<Notification> fetchNotificationsByContents(@RequestParam("contents") String contents) {
        return service.fetchNotificationsContents(contents).as(this::validation);
    }

    private <T> Mono<T> validation(Mono<T> publisher) {
        return publisher.switchIfEmpty(Mono.error(new NotFoundException()));
    }

    private <T> Flux<T> validation(Flux<T> publisher) {
        return publisher.switchIfEmpty(Flux.error(new NotFoundException()));
    }
}
