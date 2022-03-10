package ru.red.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.red.domain.UserBilling;
import ru.red.dto.UserIdentityDTO;
import ru.red.repository.UserBillingRepository;

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
        return repository.save(domain);
    }

    @Override
    public Mono<UserBilling> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Mono<UserBilling> findByEmail(String email) {
        return repository.findByEmail(email);
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
                .flatMap(repository::save);
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
                .flatMap(repository::save);
    }
}
