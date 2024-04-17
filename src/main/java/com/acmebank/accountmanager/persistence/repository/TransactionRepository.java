package com.acmebank.accountmanager.persistence.repository;

import com.acmebank.accountmanager.persistence.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}