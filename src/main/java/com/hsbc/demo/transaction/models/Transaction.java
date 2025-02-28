package com.hsbc.demo.transaction.models;

import lombok.Data;


@Data
public class Transaction {
    /**
     * 交易ID
     */
    private Long id;

    /**
     * 客户端上传的uuid，避免重复提交创建多个订单
     */
    private String clientTransactioId;
    /**
     * 来源账户ID
     */
    private String fromAccountId;
    /**
     * 目标账户ID
     */
    private String toAccountId;
    /**
     * 交易金额，以币种最小单位计算
     */
    private Long amount;
    /**
     * 币种
     */
    private String currency;

    /**
     * 备注
     */
    private String remark;

    /**
     * UTC时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 订单状态：0 未支付，1 已支付，2 已取消, 3 已失败
     */
    private Integer status;
    /**
     * 乐观锁
     */
    private Long version;


}
