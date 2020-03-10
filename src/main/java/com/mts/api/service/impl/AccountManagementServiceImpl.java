package com.mts.api.service.impl;

import com.mts.api.domain.Account;
import com.mts.api.domain.User;
import com.mts.api.exceptions.impl.AccountDoesNotExist;
import com.mts.api.exceptions.impl.InsufficientBalance;
import com.mts.api.repository.AccountRepository;
import com.mts.api.service.AccountManagementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Slf4j
@Service
@AllArgsConstructor
public class AccountManagementServiceImpl implements AccountManagementService {
    private final AccountRepository accountRepository;

    @Override
    public Account createAccount(String email, String username) {
        return accountRepository.save(Account.builder()
                .userId(User.builder()
                        .username(username)
                        .email(email)
                        .build())
                .balance(BigDecimal.ZERO)
                .build());
    }

    @Override
    public Account withdrawMoney(BigDecimal amount, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountDoesNotExist(accountId));
        if (account.getBalance().compareTo(amount) > -1) {
            account.setBalance(account.getBalance().subtract(amount));
            return accountRepository.save(account);
        } else {
            throw new InsufficientBalance(amount);
        }
    }

    @Override
    public Account depositMoney(BigDecimal amount, Long accountId) {
        return accountRepository.findById(accountId)
                .map(account -> {
                    account.setBalance(account.getBalance().add(amount));
                    return accountRepository.save(account);
                }).orElseThrow(() -> new AccountDoesNotExist(accountId));
    }

    @Transactional
    @Override
    public void transferMoney(BigDecimal amount, Long fromAccountId, Long toAccountId) {
        withdrawMoney(amount, fromAccountId);
        depositMoney(amount, toAccountId);
    }

}
