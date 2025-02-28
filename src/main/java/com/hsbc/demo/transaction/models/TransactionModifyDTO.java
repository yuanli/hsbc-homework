package com.hsbc.demo.transaction.models;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionModifyDTO {
    @NotNull
    private Long transactionId;
    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
    private Long version;
}
