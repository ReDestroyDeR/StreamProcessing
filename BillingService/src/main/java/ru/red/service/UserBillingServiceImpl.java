package ru.red.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import ru.red.domain.UserBilling;
import ru.red.dto.UserIdentityDTO;
import ru.red.exception.BadRequestException;
import ru.red.exception.NotFoundException;
import ru.red.repository.UserBillingRepository;

@Log4j2
@Service
public class UserBillingServiceImpl implements UserBillingService {
    private final UserBillingRepository repository;

    @Autowired
    public UserBillingServiceImpl(UserBillingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<UserBilling> create(UserIdentityDTO dto) {
        var domain = new UserBilling();
        domain.setEmail(dto.getEmail());
        domain.setBalance(0);
        return repository.save(domain)
                .onErrorMap(BadRequestException::new) // TODO: Ignore connection exceptions
                .log((Logger) log);
    }

    @Override
    public Mono<UserBilling> findById(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("No user by id %s".formatted(id))))
                .log((Logger) log);
    }

    @Override
    public Mono<UserBilling> findByEmail(String email) {
        return repository.findByEmail(email)
                .switchIfEmpty(Mono.error(new NotFoundException("No user for email %s".formatted(email))))
                .log((Logger) log);
    }

    @Override
    public Mono<UserBilling> addFundsToUser(String email, int add) {
        return findByEmail(email)
                .map(billing -> {
                    billing.setBalance(
                            billing.getBalance() + add
                    );
                    return billing;
                })
                .as(this::billingBalanceValidation)
                .flatMap(repository::save)
                .log((Logger) log);
    }

    @Override
    public Mono<UserBilling> removeFundsFromUser(String email, int sub) {
        return findByEmail(email)
                .map(billing -> {
                    billing.setBalance(
                            billing.getBalance() - sub
                    );
                    return billing;
                })
                .as(this::billingBalanceValidation)
                .flatMap(repository::save)
                .log((Logger) log);
    }

    private Mono<UserBilling> billingBalanceValidation(Mono<UserBilling> billingMono) {
        return billingMono.flatMap(billing -> billing.getBalance() < 0
                ? Mono.error(new BadRequestException("Negative balance"))
                : billingMono);
    }
}
