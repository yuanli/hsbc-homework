package com.hsbc.demo.transaction.controller;

import com.hsbc.demo.transaction.models.TransactionCreateDTO;
import com.hsbc.demo.transaction.models.TransactionDTO;
import com.hsbc.demo.transaction.models.TransactionModifyDTO;
import com.hsbc.demo.transaction.models.common.Page;
import com.hsbc.demo.transaction.service.TransactionService;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/transaction")
@RestController
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    TransactionDTO create(@Validated @RequestBody TransactionCreateDTO transaction) {
        return transactionService.create(transaction);
    }

    @GetMapping("/query")
    public Page<TransactionDTO> query(@RequestParam("lastTransactionId") @Nullable Long lastTransactionId,
                                      @Range(min = 1, max = 100, message = "pageSize参数不合法")
                                      @RequestParam("pageSize") Integer pageSize) {
        if (lastTransactionId == null) {
            lastTransactionId = 0L;
        }
        return transactionService.query(lastTransactionId, pageSize);
    }

    @GetMapping("/getById")
    public TransactionDTO getById(@RequestParam("id") @Validated @NotNull Long id) {
        return transactionService.getById(id);
    }


    @PutMapping("/modify")
    void modify(@Validated @RequestBody TransactionModifyDTO transaction) {
        transactionService.modify(transaction);
    }

    @DeleteMapping("/delete")
    void delete(@Validated @NotNull @RequestParam("id") Long transactionId) {
        transactionService.delete(transactionId);
    }
}
