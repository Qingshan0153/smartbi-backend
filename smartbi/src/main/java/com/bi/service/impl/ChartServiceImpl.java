package com.bi.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bi.common.ErrorCode;
import com.bi.constant.CommonConstant;
import com.bi.exception.BusinessException;
import com.bi.exception.ThrowUtils;
import com.bi.manager.AiManager;
import com.bi.manager.RedisLimiterManager;
import com.bi.mapper.ChartMapper;
import com.bi.model.dto.chart.ChartQueryRequest;
import com.bi.model.entity.Chart;
import com.bi.model.vo.BiResponse;
import com.bi.service.ChartService;
import com.bi.utils.ExcelUtils;
import com.bi.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author mendax
 * @description 针对表【chart(图标信息表)】的数据库操作Service实现
 * @createDate 2023-09-21 21:01:08
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {

    final List<String> fileSuffixWriteList = Arrays.asList( "xlsx", "xls");

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BiResponse getBiResponseInfo(MultipartFile multipartFile, Long userId, String goal, String chartName, String chartType) {

        long oneMb = 1024 * 1024;

        // 校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > oneMb, ErrorCode.PARAMS_ERROR, "文件大小超出1MB");
        // 校验文件后缀
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!fileSuffixWriteList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
        final String limitPrefix = "ai_request_";
        // 用户调用次数限流(限流粒度明确：单个用户单位时间内访问的限流)
        redisLimiterManager.doRateLimit(limitPrefix + userId);

        // 读取用户上传文件
        String result = ExcelUtils.excelToCsv(multipartFile);

//        String aiContent = aiManager.doChat(goal, result, chartType);
        String aiContent = "null";

        String[] split = aiContent.split("【【【【【");
        if (split.length < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI 信息生成错误");
        }
        String genChart = split[1].trim();
        String genResult = split[2].trim();

        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartName(chartName);
        chart.setChartData(result);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(userId);
        this.save(chart);
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        return biResponse;
    }
}




