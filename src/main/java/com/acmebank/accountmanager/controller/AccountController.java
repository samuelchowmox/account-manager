package com.acmebank.accountmanager.controller;

import com.acmebank.accountmanager.dto.AccountResponseDto;
import com.acmebank.accountmanager.persistence.model.Account;
import com.acmebank.accountmanager.persistence.model.Transaction;
import com.acmebank.accountmanager.persistence.repository.AccountRepository;
import com.acmebank.accountmanager.persistence.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private static final String TRANSFER_SUCCESSFUL = "Transfer successful";
    private static final String TRANSFER_NOT_ENOUGH_BALANCE = "Not enough balance";
    private static final String TRANSFER_INVALID_ACCOUNT = "Invalid account";
    private static final String TRANSFER_INVALID_AMOUNT = "Invalid amount";

    @RequestMapping(value = "/getAccountBalance", method = RequestMethod.GET)
    public ResponseEntity<AccountResponseDto> getAccountBalance(@RequestParam(value = "accountId") Long accountId) {
        ResponseEntity<AccountResponseDto> response = null;
        Optional<Account> account = accountRepository.findById(accountId);

        // Valid account number
        if (account.isPresent()) {
            AccountResponseDto accountResponseDto = new AccountResponseDto();
            accountResponseDto.setAccountId(account.get().getId());
            accountResponseDto.setBalance(account.get().getBalance());

            response = new ResponseEntity<>(accountResponseDto, HttpStatus.OK);
        }
        // Invalid account number
        else {
            response = new ResponseEntity<>(null, HttpStatus.OK);
        }
        return response;
    }

    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public ResponseEntity<String> transfer(@RequestParam(value = "fromAccountId") Long fromAccountId,
                                           @RequestParam(value = "toAccountId") Long toAccountId,
                                           @RequestParam(value = "transferAmount") Double transferAmount) {
        Optional<Account> fromAccount;
        Optional<Account> toAccount;

        // Check invalid account
        fromAccount = accountRepository.findById(fromAccountId);
        toAccount = accountRepository.findById(toAccountId);

        if (fromAccount.isEmpty() || toAccount.isEmpty()) {
            return new ResponseEntity<>(TRANSFER_INVALID_ACCOUNT, HttpStatus.OK);
        }

        // Check invalid amount
        if (transferAmount <= 0) {
            return new ResponseEntity<>(TRANSFER_INVALID_AMOUNT, HttpStatus.OK);
        }

        // Check enough balance
        if (fromAccount.get().getBalance() < transferAmount) {
            return new ResponseEntity<>(TRANSFER_NOT_ENOUGH_BALANCE, HttpStatus.OK);
        }

        transferAmount(fromAccount.get(), toAccount.get(), transferAmount);
        return new ResponseEntity<>(TRANSFER_SUCCESSFUL, HttpStatus.OK);
    }

    @Transactional
    public void transferAmount(Account fromAccount, Account toAccount, Double transferAmount) {
        fromAccount.setBalance(fromAccount.getBalance() - transferAmount);
        toAccount.setBalance(toAccount.getBalance() + transferAmount);

        Transaction transaction = new Transaction();
        transaction.setFromAccountId(fromAccount.getId());
        transaction.setToAccountId(toAccount.getId());
        transaction.setAmount(transferAmount);
        transaction.setCreatedTime(new Date());

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        transactionRepository.save(transaction);
    }
}