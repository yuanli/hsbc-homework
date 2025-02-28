package com.hsbc.demo.transaction.dao;

import com.hsbc.demo.transaction.models.Transaction;
import com.hsbc.demo.transaction.models.TransactionDTO;
import com.hsbc.demo.transaction.models.TransactionModifyDTO;
import com.hsbc.demo.transaction.models.common.ApiErrorCode;
import com.hsbc.demo.transaction.models.common.ApiException;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.util.Strings;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.hsbc.demo.transaction.models.common.ApiErrorCode.ERR_DATA_HAS_BEEN_MODIFIED;
import static com.hsbc.demo.transaction.models.common.ApiErrorCode.ERR_INVALID_TRANSACTION_ID;

/**
 * @author Administrator
 */
@Repository
public class TransactionDao {
    private final JdbcTemplate jdbcTemplate;

    public TransactionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean create(Transaction transaction) {
        String sql = """
                INSERT INTO transaction (
                         id, 
                         client_transaction_id, 
                         from_account_id, 
                         to_account_id, 
                         amount, 
                         currency,
                         remark, 
                         create_time, 
                         update_time, 
                         status, 
                         version)
                SELECT ?,?,?,?,?,?,?,?,?,?,?
                WHERE NOT EXISTS (SELECT 1 FROM transaction WHERE client_transaction_id = ? and from_account_id = ?);
                """;
        int rowInserted = jdbcTemplate.update(sql,
                transaction.getId(),
                transaction.getClientTransactioId(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getRemark(),
                transaction.getCreateTime(),
                transaction.getUpdateTime(),
                transaction.getStatus(),
                transaction.getVersion(),
                transaction.getClientTransactioId(),
                transaction.getFromAccountId());
        return rowInserted > 0;
    }

    public boolean delete(Long transactionId) {
        int rowDeleted = jdbcTemplate.update("delete from transaction where id = ?", transactionId);
        return rowDeleted == 1;
    }

    @Transactional
    public void modify(TransactionModifyDTO transactionDto) {
        String sql1 = "select version from transaction where id = ?";
        Long curVersion = jdbcTemplate.queryForObject(sql1, Long.class, transactionDto.getTransactionId());

        if (curVersion == null) {
            throw new ApiException(ERR_INVALID_TRANSACTION_ID);
        }
        if (!curVersion.equals(transactionDto.getVersion())) {
            throw new ApiException(ERR_DATA_HAS_BEEN_MODIFIED);
        }

        // 动态拼接sql
        List<String> paramNames = new ArrayList<>();
        List<Object> paramValues = new ArrayList<>();
        if (transactionDto.getStatus() != null) {
            paramNames.add(" status = ? ");
            paramValues.add(transactionDto.getStatus());
        }

        if (transactionDto.getRemark() != null) {
            paramNames.add(" remark = ? ");
            paramValues.add(transactionDto.getRemark());
        }

        if (paramNames.isEmpty()) {
            throw new ApiException(ApiErrorCode.ERR_INVALID_PARAMS);
        }
        paramValues.add(transactionDto.getTransactionId());
        paramValues.add(curVersion);
        String sql = "update transaction set " + Strings.join(paramNames, ',') + ", version = version+1 where id = ? and version = ?";
        jdbcTemplate.update(sql, paramValues.toArray());
    }

    public Integer queryCnt(@NotNull Long lastTransactionId) {
        String sql = "select count(1) from transaction where id > ? ;";
        return jdbcTemplate.queryForObject(sql, Integer.class, lastTransactionId);
    }

    public List<TransactionDTO> query(@NotNull Long lastTransactionId, Integer pageSize) {

        String sql = """
                select id, 
                    client_transaction_id, 
                    from_account_id, 
                    to_account_id, 
                    amount, 
                    currency, 
                    remark, 
                    create_time, 
                    update_time, 
                    status, 
                    version
                from transaction 
                where id > ? 
                order by id asc 
                limit ?;""";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TransactionDTO.class), lastTransactionId, pageSize);
    }


    public TransactionDTO getById(@NotNull Long id) {

        String sql = """
                select id, 
                    client_transaction_id, 
                    from_account_id, 
                    to_account_id, 
                    amount, 
                    currency, 
                    remark, 
                    create_time, 
                    update_time, 
                    status, 
                    version
                from `transaction` 
                where id = ? 
                """;
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(TransactionDTO.class), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


}
