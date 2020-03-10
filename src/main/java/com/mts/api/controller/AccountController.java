package com.mts.api.controller;

import com.mts.api.domain.Account;
import com.mts.api.service.AccountManagementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/account")
public class AccountController {
    private final AccountManagementService accountManagementService;

    @PostMapping(value = "/create")
    public Account createAccount(@RequestParam @Valid String email, @RequestParam String username) {
        log.info("creating account for email {} ", email);
        return accountManagementService.createAccount(email, username);
    }

    @PutMapping(value = "/withdraw")
    public void withdrawAccount(@RequestParam @Positive BigDecimal amount, @RequestParam Long accountId) {
        log.info("withdrawal amount {} from account {} ", amount, accountId);
        accountManagementService.withdrawMoney(amount, accountId);
    }

    @PutMapping(value = "/deposit")
    public void depositAccount(@RequestParam @Positive BigDecimal amount, @RequestParam Long accountId) {
        accountManagementService.depositMoney(amount, accountId);
    }

    @PostMapping(value = "/transfer")
    public void transferMoney(@RequestParam @Positive BigDecimal amount,
                              @RequestParam Long fromAccount,
                              @RequestParam Long toAccount) {
        accountManagementService.transferMoney(amount, fromAccount, toAccount);
    }

}
