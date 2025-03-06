package com.microservices.smmsb_transaction_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;  
import org.springframework.stereotype.Repository;
import com.microservices.smmsb_transaction_service.model.Transaction;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

}