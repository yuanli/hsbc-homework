package com.hsbc.demo.transaction.config;

import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IdGenerator {
    @Value("${uuid.workerId}")
    private short workerId;

    @PostConstruct
    public void initBean() {
        IdGeneratorOptions options = new IdGeneratorOptions();

        if (workerId > (2 ^ options.WorkerIdBitLength - 1) || workerId <= 0) {
            throw new IllegalStateException("worker id number out of range");
        }

        log.info("snowflake worker id number: {}", workerId);

        options.WorkerId = workerId;
        YitIdHelper.setIdGenerator(options);
    }

    public long nextId() {
        return YitIdHelper.nextId();
    }
}
