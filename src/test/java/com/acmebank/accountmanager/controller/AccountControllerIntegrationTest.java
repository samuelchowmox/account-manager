package com.acmebank.accountmanager.controller;

import com.acmebank.accountmanager.Application;
import com.acmebank.accountmanager.persistence.model.Transaction;
import com.acmebank.accountmanager.persistence.repository.TransactionRepository;
import org.json.JSONException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountControllerIntegrationTest {
    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    @Autowired
    TransactionRepository transactionRepository;

    @Test
    @Order(1)
    public void testGetAccountBalanceReturnAccountIdAndBalance() throws JSONException {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("accountId", "88888888");

        String url = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/api/v1/account/getAccountBalance"))
                .queryParams(queryParams)
                .toUriString();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        String expected = "{\"accountId\":88888888,\"balance\":1000000.0}";

        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    @Order(2)
    public void testGetAccountBalanceNotReturnAccountIdAndBalance() throws JSONException {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("accountId", "87654321");

        String url = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/api/v1/account/getAccountBalance"))
                .queryParams(queryParams)
                .toUriString();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        JSONAssert.assertEquals(null, response.getBody(), false);
    }

    @Test
    @Order(3)
    public void testTransferInvalidAccount() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("fromAccountId", "87654321");
        queryParams.add("toAccountId", "88888888");
        queryParams.add("transferAmount", "1");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(queryParams, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v1/account/transfer"),
                HttpMethod.POST, entity, String.class);

        String actual = response.getBody();
        assertEquals("Invalid account", actual);
    }

    @Test
    @Order(4)
    public void testTransferNotEnoughBalance() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("fromAccountId", "12345678");
        queryParams.add("toAccountId", "88888888");
        queryParams.add("transferAmount", "9999999");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(queryParams, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v1/account/transfer"),
                HttpMethod.POST, entity, String.class);

        String actual = response.getBody();
        assertEquals("Not enough balance", actual);
    }

    @Test
    @Order(5)
    public void testTransferTransferSuccessful() throws JSONException {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("fromAccountId", "12345678");
        queryParams.add("toAccountId", "88888888");
        queryParams.add("transferAmount", "1");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(queryParams, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/v1/account/transfer"),
                HttpMethod.POST, entity, String.class);

        String actual = response.getBody();
        assertEquals("Transfer successful", actual);

        // Verify account 12345678 balance
        queryParams.clear();
        queryParams.add("accountId", "12345678");

        String url = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/api/v1/account/getAccountBalance"))
                .queryParams(queryParams)
                .toUriString();

        response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        String expected = "{\"accountId\":12345678,\"balance\":999999.0}";

        JSONAssert.assertEquals(expected, response.getBody(), false);

        // Verify account 88888888 balance
        queryParams.clear();
        queryParams.add("accountId", "88888888");

        url = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/api/v1/account/getAccountBalance"))
                .queryParams(queryParams)
                .toUriString();

        response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        expected = "{\"accountId\":88888888,\"balance\":1000001.0}";

        JSONAssert.assertEquals(expected, response.getBody(), false);

        //Verify transaction table
        Optional<Transaction> transaction = transactionRepository.findById(1L);
        assertEquals(12345678L, transaction.map(Transaction::getFromAccountId).orElse(null));
        assertEquals(88888888L, transaction.map(Transaction::getToAccountId).orElse(null));
        assertEquals(Double.valueOf(1), transaction.map(Transaction::getAmount).orElse(null));
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}