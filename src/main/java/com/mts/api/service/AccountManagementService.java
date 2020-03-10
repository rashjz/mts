package com.mts.api.service;

import com.mts.api.domain.Account;

import java.math.BigDecimal;

public interface AccountManagementService {
    Account createAccount(String email, String username);

    Account withdrawMoney(BigDecimal amount, Long accountId);

    Account depositMoney(BigDecimal amount, Long accountId);

    void transferMoney(BigDecimal amount, Long fromAccount, Long toAccount);
}
