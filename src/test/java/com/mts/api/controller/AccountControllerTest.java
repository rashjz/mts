package com.mts.api.controller;

import com.mts.api.domain.Account;
import com.mts.api.domain.User;
import com.mts.api.exceptions.impl.AccountDoesNotExist;
import com.mts.api.exceptions.impl.InsufficientBalance;
import com.mts.api.service.AccountManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link AccountController}
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AccountControllerTest {

    private static final String USER_EMAIL = "test@mail.com";
    private static final String USERNAME = "testUser";
    private static final BigDecimal ACCOUNT_BALANCE = BigDecimal.TEN;
    private static final Long ACCOUNT_ID = 10L;

    @MockBean
    private AccountManagementService accountManagementService;
    @Autowired
    private MockMvc mockMvc;
    @Captor
    private ArgumentCaptor<BigDecimal> amountCaptor;

    @Test
    public void testCreateAccountCheckExpectedResponse() throws Exception {
        when(accountManagementService.createAccount(anyString(), anyString()))
                .thenReturn(createAccount());

        this.mockMvc.perform(post("/account/create")
                .contentType(APPLICATION_JSON)
                .param("email", USER_EMAIL)
                .param("username", USERNAME)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.userId.username").value(USERNAME))
                .andExpect(jsonPath("$.accountId").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.balance").value(ACCOUNT_BALANCE))
                .andExpect(status().isOk());
    }

    @Test
    public void testWithdrawMoneyWithInsufficientBalanceCheckErrorStatus() throws Exception {
        BigDecimal amount = new BigDecimal(200);
        when(accountManagementService.withdrawMoney(eq(amount), eq(ACCOUNT_ID)))
                .thenThrow(new InsufficientBalance(amount));

        this.mockMvc.perform(put("/account/withdraw")
                .contentType(APPLICATION_JSON)
                .param("amount", amount.toString())
                .param("accountId", ACCOUNT_ID.toString())
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\":{\"status\":404,\"reason\":\"ACCOUNT_LIST_LIMIT_REACHED\",\"message\":\"There isn't enough balance: 200\"}}"));
        //verify
        verify(accountManagementService, times(1)).withdrawMoney(eq(amount), eq(ACCOUNT_ID));
    }

    @Test
    public void testWithdrawMoneyWithValidBalanceMustReturnOk() throws Exception {
        BigDecimal amount = BigDecimal.TEN;
        when(accountManagementService.withdrawMoney(any(), any()))
                .thenReturn(createAccount());

        this.mockMvc.perform(put("/account/withdraw")
                .contentType(APPLICATION_JSON)
                .param("amount", ACCOUNT_BALANCE.toString())
                .param("accountId", ACCOUNT_ID.toString())
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        //verify if service called
        verify(accountManagementService, times(1)).withdrawMoney(eq(amount), eq(ACCOUNT_ID));
    }

    @Test
    public void testDepositMoneyOnValidAccountCheckStatusIsOk() throws Exception {
        BigDecimal deposit = new BigDecimal(20);
        Account account = createAccount();
        when(accountManagementService.depositMoney(amountCaptor.capture(), eq(ACCOUNT_ID)))
                .thenReturn(account);

        this.mockMvc.perform(put("/account/deposit")
                .contentType(APPLICATION_JSON)
                .param("amount", deposit.toString())
                .param("accountId", ACCOUNT_ID.toString())
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        assertEquals(deposit, amountCaptor.getValue());
    }

    @Test
    public void testDepositMoneyOnInvalidAccountIdCheckErrorStatus() throws Exception {
        BigDecimal amount = BigDecimal.ZERO;
        when(accountManagementService.depositMoney(eq(amount), eq(ACCOUNT_ID)))
                .thenThrow(new AccountDoesNotExist(ACCOUNT_ID));

        MvcResult result = this.mockMvc.perform(put("/account/deposit")
                .contentType(APPLICATION_JSON)
                .param("amount", amount.toString())
                .param("accountId", ACCOUNT_ID.toString())
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        String expectedErrorMessage = String.format("Account does not exist: %s", ACCOUNT_ID);
        assertEquals(expectedErrorMessage, result.getResolvedException().getMessage());
    }

    @Test
    public void testTransferMoneyOnInsufficientBalanceCheckErrorStatus() throws Exception {
        long fromAccountId = 1L;
        long toAccountId = 2L;

        doThrow(new InsufficientBalance(ACCOUNT_BALANCE))
                .when(accountManagementService).transferMoney(eq(BigDecimal.TEN), eq(fromAccountId), eq(toAccountId));

        this.mockMvc.perform(post("/account/transfer")
                .contentType(APPLICATION_JSON)
                .param("amount", BigDecimal.TEN.toString())
                .param("fromAccount", Long.toString(fromAccountId))
                .param("toAccount", Long.toString(toAccountId))
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\":{\"status\":404,\"reason\":\"ACCOUNT_LIST_LIMIT_REACHED\",\"message\":\"There isn't enough balance: 10\"}}"));
    }

    private Account createAccount() {
        return Account.builder()
                .accountId(ACCOUNT_ID)
                .userId(User.builder()
                        .email(USER_EMAIL)
                        .username(USERNAME)
                        .build())
                .balance(ACCOUNT_BALANCE)
                .build();
    }
}
