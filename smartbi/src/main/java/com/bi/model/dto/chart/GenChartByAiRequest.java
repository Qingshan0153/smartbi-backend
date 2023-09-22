package com.bi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * @author mendax
 * @version 2023/9/22 15:29
 */

@Data
public class GenChartByAiRequest implements Serializable {


    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;


}
