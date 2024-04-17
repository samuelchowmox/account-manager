package com.acmebank.accountmanager.controller;

import com.acmebank.accountmanager.persistence.model.Account;
import com.acmebank.accountmanager.persistence.repository.AccountRepository;
import com.acmebank.accountmanager.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private TransactionRepository transactionRepository;

    @Test
    public void shouldReturnAccountIdAndBalance() throws Exception {
        Account account = new Account(88888888L, Double.valueOf("1000000"), new Date());
        given(accountRepository.findById(88888888L)).willReturn(Optional.of(account));

        mockMvc.perform(get("/api/v1/account/getAccountBalance").param("accountId", "88888888"))
                .andExpect(jsonPath("$.accountId").value(88888888))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void shouldNotReturnCorrectAccountIdAndBalance() throws Exception {
        Account account = new Account(88888888L, Double.valueOf("1000000"), new Date());
        given(accountRepository.findById(88888888L)).willReturn(Optional.of(account));

        mockMvc.perform(get("/api/v1/account/getAccountBalance").param("accountId", "87654321"))
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void shouldReturnInvalidAccount() throws Exception {
        Account fromAccount = new Account(12345678L, Double.valueOf("1000000"), new Date());
        given(accountRepository.findById(12345678L)).willReturn(Optional.of(fromAccount));

        Account toAccount = new Account(88888888L, Double.valueOf("1000000"), new Date());
        given(accountRepository.findById(88888888L)).willReturn(Optional.of(toAccount));

        mockMvc.perform(post("/api/v1/account/transfer")
                        .param("fromAccountId", "87654321")
                        .param("toAccountId", "88888888")
                        .param("transferAmount", "1"))
                .andExpect(content().string("Invalid account"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void shouldReturnNotEnoughBalance() throws Exception {
        Account fromAccount = new Account(12345678L, Double.valueOf("1000000"), new Date());
        given(accountRepository.findById(12345678L)).willReturn(Optional.of(fromAccount));

        Account toAccount = new Account(88888888L, Double.valueOf("1000000"), new Date());
        given(accountRepository.findById(88888888L)).willReturn(Optional.of(toAccount));

        mockMvc.perform(post("/api/v1/account/transfer")
                        .param("fromAccountId", "12345678")
                        .param("toAccountId", "88888888")
                        .param("transferAmount", "9999999"))
                .andExpect(content().string("Not enough balance"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void shouldReturnTransferSuccessful() throws Exception {
        Account fromAccount = new Account(12345678L, Double.valueOf("1000000"), new Date());
        given(accountRepository.findById(12345678L)).willReturn(Optional.of(fromAccount));

        Account toAccount = new Account(88888888L, Double.valueOf("1000000"), new Date());
        given(accountRepository.findById(88888888L)).willReturn(Optional.of(toAccount));

        mockMvc.perform(post("/api/v1/account/transfer")
                        .param("fromAccountId", "12345678")
                        .param("toAccountId", "88888888")
                        .param("transferAmount", "1"))
                .andExpect(content().string("Transfer successful"))
                .andExpect(status().is2xxSuccessful());
    }
}