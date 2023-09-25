package com.bi.model.vo;

import lombok.Data;

/**
 * BI 返回结果
 *
 * @author mendax
 * @version 2023/9/23 16:30
 */

@Data
public class BiResponse {

    private Long chartId;
    private String genChart;
    private String genResult;

}
