package com.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bi.model.dto.chart.ChartQueryRequest;
import com.bi.model.entity.Chart;
import com.bi.model.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

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


    /**
     * 获取 Ai 生成 信息
     *
     * @param multipartFile 数据文件
     * @param userId        用户id
     * @param goal          生成目标
     * @param chartName     图表名称
     * @param chartType     图表类型
     * @return BiResponse
     */
    BiResponse getBiResponseInfo(MultipartFile multipartFile, Long userId, String goal, String chartName, String chartType);
}
