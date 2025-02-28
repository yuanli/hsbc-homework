package com.hsbc.demo.transaction.models;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreateDTO {

    /**
     * 客户端上传的uuid，避免重复提交创建多个订单
     */
    @NotNull
    private String clientTransactionId;

    /**
     * 来源账户ID
     */
    @NotNull
    private String fromAccountId;
    /**
     * 目标账户ID
     */
    @NotNull
    private String toAccountId;
    /**
     * 交易金额，以币种最小单位计算
     */
    @NotNull
    private Long amount;
    /**
     * 币种
     */
    @NotNull
    private String currency;

    /**
     * 备注
     */
    private String remark;

}
