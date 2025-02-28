package com.hsbc.demo.transaction.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Component
public class RunAfterInited implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public RunAfterInited(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS transaction (
                    id BIGINT PRIMARY KEY,
                    client_transaction_id VARCHAR(255) NOT NULL,
                    from_account_id VARCHAR(255) NOT NULL,
                    to_account_id VARCHAR(255) NOT NULL,
                    amount BIGINT NOT NULL,
                    currency VARCHAR(255) NOT NULL,
                    remark VARCHAR(255),
                    create_time BIGINT,
                    update_time BIGINT,
                    status INT,
                    version BIGINT DEFAULT 0,
                    UNIQUE KEY unique_client_transaction_id_from_account_id (client_transaction_id, from_account_id)
                );
                """);
    }
}
