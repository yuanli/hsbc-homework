package com.hsbc.demo.transaction.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.hsbc.demo.transaction.config.IdGenerator;
import com.hsbc.demo.transaction.dao.TransactionDao;
import com.hsbc.demo.transaction.models.Transaction;
import com.hsbc.demo.transaction.models.TransactionCreateDTO;
import com.hsbc.demo.transaction.models.TransactionDTO;
import com.hsbc.demo.transaction.models.TransactionModifyDTO;
import com.hsbc.demo.transaction.models.common.ApiException;
import com.hsbc.demo.transaction.models.common.BizConstants;
import com.hsbc.demo.transaction.models.common.Page;
import com.hsbc.demo.transaction.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.hsbc.demo.transaction.models.common.ApiErrorCode.ERR_INVALID_TRANSACTION_ID;
import static com.hsbc.demo.transaction.models.common.ApiErrorCode.ERR_TRANSACTION_EXISTS;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final BloomFilter<Long> transactionIdBloomFilter = BloomFilter.create(
            Funnels.longFunnel(),
            1000 * 10,
            0.01);

    private final TransactionDao transactionDao;
    private final IdGenerator idGenerator;
    private final Cache<Long, String> cache;
    private final ObjectMapper objectMapper;

    public TransactionServiceImpl(TransactionDao transactionDao, IdGenerator idGenerator, Cache<Long, String> cache, ObjectMapper objectMapper) {
        this.transactionDao = transactionDao;
        this.idGenerator = idGenerator;
        this.cache = cache;
        this.objectMapper = objectMapper;
    }


    /**
     * 物理分页查询所有记录
     */
    public Page<TransactionDTO> query(Long lastTransactionId, Integer pageSize) {
        // 如果没有传入lastTransactionId，则从数据库中获取最后一条记录的id
        if (lastTransactionId == null) {
            lastTransactionId = 0L;
        }
        List<TransactionDTO> datas = transactionDao.query(lastTransactionId, pageSize);
        datas.forEach(transactionDTO -> {
            try {
                cache.put(transactionDTO.getId(), objectMapper.writeValueAsString(transactionDTO));
            } catch (Exception e) {
                // 添加缓存失败，仅记录，不做特殊处理
                log.info("添加缓存失败：{}", transactionDTO, e);
            }
        });
        return new Page<>(transactionDao.queryCnt(lastTransactionId), datas);
    }

    @Override
    public TransactionDTO getById(Long transactionId) {
        String cacheValue = cache.getIfPresent(transactionId);
        if (cacheValue != null) {
            try {
                return objectMapper.readValue(cacheValue, TransactionDTO.class);
            } catch (JsonProcessingException e) {
                log.info("read transaction from cache failed, {}", transactionId, e);
            }
        }

        if (!transactionIdBloomFilter.mightContain(transactionId)) {
            throw new ApiException(ERR_INVALID_TRANSACTION_ID);
        }

        TransactionDTO value = transactionDao.getById(transactionId);
        if (null != value) {
            try {
                cache.put(transactionId, objectMapper.writeValueAsString(value));
            } catch (JsonProcessingException e) {
                log.info("catch transaction failed, {}", transactionId, e);
            }
        } else {
            throw new ApiException(ERR_INVALID_TRANSACTION_ID);
        }
        return value;
    }

    private long getUTCTimestamp() {
        LocalDateTime now = LocalDateTime.now();

        // 转换为UTC时间
        ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));

        // 转换为时间戳
        Instant instant = zonedDateTime.toInstant();
        return instant.toEpochMilli();
    }

    @Override
    public TransactionDTO create(TransactionCreateDTO dto) {
        Transaction transaction = new Transaction();

        transaction.setId(idGenerator.nextId());
        transaction.setClientTransactioId(dto.getClientTransactionId());
        transaction.setFromAccountId(dto.getFromAccountId());
        transaction.setToAccountId(dto.getToAccountId());
        transaction.setAmount(dto.getAmount());
        transaction.setCurrency(dto.getCurrency());
        transaction.setRemark(dto.getRemark());

        long curTimeStamp = getUTCTimestamp();
        transaction.setCreateTime(curTimeStamp);
        transaction.setUpdateTime(curTimeStamp);
        transaction.setStatus(BizConstants.TRANSACTION_STATUS_CREATED);
        transaction.setVersion(1L);

        // 使用exists插入，不用显式使用事务且减少了sql数量
        if (!transactionDao.create(transaction)) {
            throw new ApiException(ERR_TRANSACTION_EXISTS);
        }
        transactionIdBloomFilter.put(transaction.getId());


        TransactionDTO ret = new TransactionDTO();
        ret.setId(transaction.getId());
        ret.setClientTransactionId(transaction.getClientTransactioId());
        ret.setFromAccountId(transaction.getFromAccountId());
        ret.setToAccountId(transaction.getToAccountId());
        ret.setAmount(transaction.getAmount());
        ret.setCurrency(transaction.getCurrency());
        ret.setRemark(transaction.getRemark());
        ret.setCreateTime(transaction.getCreateTime());
        ret.setUpdateTime(transaction.getUpdateTime());
        ret.setStatus(transaction.getStatus());
        ret.setVersion(transaction.getVersion());

        try {
            cache.put(transaction.getId(), objectMapper.writeValueAsString(ret));
        } catch (JsonProcessingException e) {
            log.error("cache transaction failed， transaction = {}", transaction, e);
        }

        return ret;
    }

    @Override
    public void delete(Long transactionId) {
        if (!transactionIdBloomFilter.mightContain(transactionId)) {
            throw new ApiException(ERR_INVALID_TRANSACTION_ID);
        }
        cache.invalidate(transactionId);

        if (!transactionDao.delete(transactionId)) {
            throw new ApiException(ERR_INVALID_TRANSACTION_ID);
        }
    }

    @Override
    public void modify(TransactionModifyDTO transactionDto) {
        if (!transactionIdBloomFilter.mightContain(transactionDto.getTransactionId())) {
            throw new ApiException(ERR_INVALID_TRANSACTION_ID);
        }

        cache.invalidate(transactionDto.getTransactionId());
        transactionDao.modify(transactionDto);
    }
}
