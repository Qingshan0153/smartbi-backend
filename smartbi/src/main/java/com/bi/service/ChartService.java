package com.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bi.model.dto.chart.ChartQueryRequest;
import com.bi.model.entity.Chart;

import javax.management.Query;

/**
 * @author mendax
 * @description 针对表【chart(图标信息表)】的数据库操作Service
 * @createDate 2023-09-21 21:01:08
 */
public interface ChartService extends IService<Chart> {

    /**
     * 获取查询条件
     *
     * @param chartQueryRequest chartQueryRequest
     * @return ueryWrapper<Chart>
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);
}
