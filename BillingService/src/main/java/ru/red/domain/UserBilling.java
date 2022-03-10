package ru.red.domain;

import lombok.Data;

@Data
public class UserBilling {
    private Long id;
    private String email;
    private Integer balance;
}
