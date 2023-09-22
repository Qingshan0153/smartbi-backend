package com.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bi.constant.CommonConstant;
import com.bi.mapper.ChartMapper;
import com.bi.model.dto.chart.ChartQueryRequest;
import com.bi.model.entity.Chart;
import com.bi.service.ChartService;
import com.bi.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author mendax
 * @description 针对表【chart(图标信息表)】的数据库操作Service实现
 * @createDate 2023-09-21 21:01:08
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {

    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {

        QueryWrapper<Chart> wrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return wrapper;
        }

        Long id = chartQueryRequest.getId();
        String chartName = chartQueryRequest.getChartName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        wrapper.eq(id != null && id > 0, "id", id);
        wrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        wrapper.like(StringUtils.isNotBlank(chartName), "chartName", chartName);
        wrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        wrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        wrapper.eq("isDelete", false);


        wrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortOrder);


        return wrapper;
    }
}




