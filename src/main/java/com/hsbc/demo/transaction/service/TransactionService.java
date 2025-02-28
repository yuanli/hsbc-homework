package com.hsbc.demo.transaction.service;

import com.hsbc.demo.transaction.models.TransactionCreateDTO;
import com.hsbc.demo.transaction.models.TransactionDTO;
import com.hsbc.demo.transaction.models.TransactionModifyDTO;
import com.hsbc.demo.transaction.models.common.Page;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService {
    TransactionDTO create(TransactionCreateDTO transaction);

    void delete(Long transactionId);

    void modify(TransactionModifyDTO transaction);

    Page<TransactionDTO> query(Long lastTransactionId, Integer pageSize);

    TransactionDTO getById(@NotNull Long transactionId);
}
