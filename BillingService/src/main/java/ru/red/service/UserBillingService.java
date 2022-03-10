package ru.red.service;

import reactor.core.publisher.Mono;
import ru.red.domain.UserBilling;
import ru.red.dto.UserBillingDTO;

public interface UserBillingService {
    Mono<UserBilling> create(UserBillingDTO dto);

    Mono<UserBilling> findById(Long id);

    Mono<UserBilling> findByEmail(String email);

    Mono<UserBilling> addFundsToUser(UserBilling billing);

    Mono<UserBilling> removeFundsFromUser(UserBilling billing);
}
