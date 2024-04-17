package com.acmebank.accountmanager.persistence.repository;

import com.acmebank.accountmanager.persistence.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}