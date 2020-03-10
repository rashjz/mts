package com.mts.api.service.impl;

import com.mts.api.domain.Account;
import com.mts.api.domain.User;
import com.mts.api.exceptions.impl.AccountDoesNotExist;
import com.mts.api.exceptions.impl.InsufficientBalance;
import com.mts.api.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link AccountManagementServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class AccountManagementServiceImplTest {
    private static final String USER_EMAIL = "test@mail.com";
    private static final String USERNAME = "testUser";
    private static final Long ACCOUNT_ID = 10L;

    @Mock
    private AccountRepository accountRepository;

    @Captor
    private ArgumentCaptor<Account> argumentCaptor;
    private AccountManagementServiceImpl accountManagementService;

    @BeforeEach
    void init() {
        accountManagementService = spy(new AccountManagementServiceImpl(accountRepository));
    }

    @Test
    @DisplayName("createAccount - check account was created with expected values")
    void testCreateAccountReturnsExpectedAccount() {
        Account expected = createAccount(BigDecimal.TEN, ACCOUNT_ID);
        doReturn(expected).when(accountRepository).save(argumentCaptor.capture());
        Account actual = accountManagementService.createAccount(USER_EMAIL, USERNAME);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("withdrawMoney - on valid account check if balance charged correctly")
    void testWithdrawMoneyOnValidAccountCheckRemainingBalance() {
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal accountBalance = new BigDecimal(20);
        Account account = createAccount(accountBalance, ACCOUNT_ID);
        when(accountRepository.findById(eq(ACCOUNT_ID))).thenReturn(Optional.of(account));

        when(accountRepository.save(argumentCaptor.capture())).thenReturn(account);
        accountManagementService.withdrawMoney(amount, ACCOUNT_ID);
        BigDecimal actualBalance = argumentCaptor.getValue().getBalance();
        BigDecimal expectedBalance = accountBalance.subtract(amount);

        assertEquals(expectedBalance, actualBalance);
    }

    @Test
    @DisplayName("withdrawMoney - on invalid account check error message")
    void testWithdrawMoneyOnInvalidAccountCheckError() {
        BigDecimal amount = BigDecimal.TEN;
        when(accountRepository.findById(eq(ACCOUNT_ID))).thenThrow(new AccountDoesNotExist(ACCOUNT_ID));

        AccountDoesNotExist thrown = assertThrows(
                AccountDoesNotExist.class,
                () -> accountManagementService.withdrawMoney(amount, ACCOUNT_ID),
                "Expected withdrawMoney() to throw, but it didn't"
        );
        //check exception message
        assertTrue(thrown.getMessage().contains(String.format("Account does not exist: %s", ACCOUNT_ID)));
    }

    @Test
    @DisplayName("withdrawMoney - on empty balance check error")
    void testWithdrawMoneyOnEmptyBalanceCheckError() {
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal balance = BigDecimal.ZERO;
        Account account = createAccount(balance, ACCOUNT_ID);
        when(accountRepository.findById(eq(ACCOUNT_ID))).thenReturn(Optional.of(account));

        InsufficientBalance thrown = assertThrows(InsufficientBalance.class, () -> accountManagementService.withdrawMoney(amount, ACCOUNT_ID));
        //check exception message
        assertTrue(thrown.getMessage().contains(String.format("There isn't enough balance: %s", amount)));
    }

    @Test
    @DisplayName("depositMoney - on invalid account check error message")
    void testDepositMoneyOnInvalidAccountCheckError() {
        BigDecimal amount = BigDecimal.TEN;
        when(accountRepository.findById(eq(ACCOUNT_ID))).thenThrow(new AccountDoesNotExist(ACCOUNT_ID));
        AccountDoesNotExist thrown = assertThrows(AccountDoesNotExist.class, () -> accountManagementService.depositMoney(amount, ACCOUNT_ID));
        //check exception message
        assertTrue(thrown.getMessage().contains(String.format("Account does not exist: %s", ACCOUNT_ID)));
    }


    @Test
    @DisplayName("depositMoney - on valid account check balance updated")
    void testDepositMoneyOnValidAccountCheckBalance() {
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal balance = new BigDecimal(101);
        Account account = createAccount(balance, ACCOUNT_ID);
        when(accountRepository.findById(eq(ACCOUNT_ID))).thenReturn(Optional.of(account));
        when(accountRepository.save(argumentCaptor.capture())).thenReturn(account);

        accountManagementService.depositMoney(amount, ACCOUNT_ID);

        BigDecimal expectedBalance = balance.add(amount);
        BigDecimal actualBalance = argumentCaptor.getValue().getBalance();
        assertEquals(expectedBalance, actualBalance);
    }

    @Test
    @DisplayName("transferMoney - if transfer succeeded check accounts balance")
    void testTransferMoneyIsSucceededCheckAccountsBalance() {
        Long fromAccountId = 1L;
        Long toAccountId = 2L;

        BigDecimal amount = BigDecimal.TEN;

        BigDecimal fromAccountBalance = new BigDecimal(101);
        BigDecimal toAccountBalance = BigDecimal.ZERO;

        Account fromAccount = createAccount(fromAccountBalance, fromAccountId);
        Account toAccount = createAccount(toAccountBalance, toAccountId);

        when(accountRepository.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(eq(toAccountId))).thenReturn(Optional.of(toAccount));

        when(accountRepository.save(argumentCaptor.capture())).thenReturn(toAccount);
        accountManagementService.transferMoney(amount, fromAccountId, toAccountId);
        BigDecimal actualFromAccountBalance = argumentCaptor.getAllValues().get(0).getBalance();
        BigDecimal actualToAccountBalance = argumentCaptor.getAllValues().get(1).getBalance();

        BigDecimal expectedFromAccountBalance = fromAccountBalance.subtract(amount);
        BigDecimal expectedToAccountBalance = toAccountBalance.add(amount);

        assertEquals(expectedFromAccountBalance, actualFromAccountBalance);
        assertEquals(expectedToAccountBalance, actualToAccountBalance);
    }

    @Test
    @DisplayName("transferMoney - fail on insufficient balance")
    void testTransferMoneyOnInsufficientBalanceCheckError() {
        BigDecimal amount = BigDecimal.TEN;
        Account account = createAccount(BigDecimal.ZERO, ACCOUNT_ID);
        Long toAccountId = 10L;
        when(accountRepository.findById(eq(ACCOUNT_ID))).thenReturn(Optional.of(account));
        InsufficientBalance thrown = assertThrows(InsufficientBalance.class,
                () -> accountManagementService.transferMoney(amount, ACCOUNT_ID, toAccountId));
        //check exception message
        assertTrue(thrown.getMessage().contains(String.format("There isn't enough balance: %s", amount)));
    }

    private Account createAccount(BigDecimal balance, Long accountId) {
        return Account.builder()
                .accountId(accountId)
                .userId(User.builder()
                        .email(USER_EMAIL)
                        .username(USERNAME)
                        .build())
                .balance(balance)
                .build();
    }
}
