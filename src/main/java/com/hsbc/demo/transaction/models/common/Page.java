package com.hsbc.demo.transaction.models.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 返回结果封装
 *
 * @param <T>
 */
@Data
@AllArgsConstructor
public class Page<T> {
    private Integer totalRecordLeft;
    private List<T> data;
}
