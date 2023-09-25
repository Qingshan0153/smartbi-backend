package com.bi.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bi.common.ErrorCode;
import com.bi.config.ThreadPoolExecutorConfig;
import com.bi.constant.CommonConstant;
import com.bi.exception.BusinessException;
import com.bi.exception.ThrowUtils;
import com.bi.manager.AiManager;
import com.bi.manager.RedisLimiterManager;
import com.bi.mapper.ChartMapper;
import com.bi.model.dto.chart.ChartQueryRequest;
import com.bi.model.entity.Chart;
import com.bi.model.enums.AsyncTaskStatusEnum;
import com.bi.model.vo.BiResponse;
import com.bi.service.ChartService;
import com.bi.utils.ExcelUtils;
import com.bi.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author mendax
 * @description 针对表【chart(图标信息表)】的数据库操作Service实现
 * @createDate 2023-09-21 21:01:08
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {

    long oneMb = 1024 * 1024L;

    final List<String> fileSuffixWriteList = Arrays.asList("xlsx", "xls");

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

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
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);


        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BiResponse getBiResponseInfo(MultipartFile multipartFile, Long userId, String goal, String chartName, String chartType) {

        // 校验文件大小
        String result = this.handleFileAndRateLimit(multipartFile, userId);

        String aiContent = aiManager.doChat(goal, result, chartType);

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
        biResponse.setChartId(chart.getId());
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        return biResponse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BiResponse getBiResponseInfoForAsync(MultipartFile multipartFile, Long userId, String goal, String chartName, String chartType) {

        String result = this.handleFileAndRateLimit(multipartFile, userId);

        // 保持用户信息到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartName(chartName);
        chart.setChartData(result);
        chart.setChartType(chartType);
        chart.setUserId(userId);
        this.save(chart);

        // 开启线程池异步执行 Ai 问询任务
        CompletableFuture.runAsync(() -> {
            // 先修改图表任务状态为 “执行中”。等执行成功后，悠改为 “已成功”、保存执行结果，执行失败后，状态修改为 “失败”，记录任务失败信息
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(AsyncTaskStatusEnum.RUNNING.getStatus());

            boolean update = this.updateById(updateChart);
            if (!update) {
                this.handleChartUpdateError(chart.getId(), "修改图表任务状态失败");
            }
            String aiContent = aiManager.doChat(goal, result, chartType);
            String[] split = aiContent.split("【【【【【");
            if (split.length < 3) {
                this.handleChartUpdateError(chart.getId(), "AI 信息生成错误");
            }
            String genChart = split[1].trim();
            String genResult = split[2].trim();
            Chart updateResultChart = new Chart();
            updateResultChart.setId(chart.getId());
            updateResultChart.setGenChart(genChart);
            updateResultChart.setStatus(AsyncTaskStatusEnum.SUCCESS.getStatus());
            updateResultChart.setGenResult(genResult);
            this.updateById(updateResultChart);

        }, threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    /**
     * 检验处理文件和请求限流逻辑处理
     *
     * @param multipartFile 文件
     * @param userId        用户id
     * @return 文件信息
     */
    @NotNull
    private String handleFileAndRateLimit(MultipartFile multipartFile, Long userId) {
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
        return ExcelUtils.excelToCsv(multipartFile);
    }

    /**
     * 处理AI图表生成错误
     *
     * @param chartId      图表id
     * @param errorMessage 错误信息
     */
    private void handleChartUpdateError(long chartId, String errorMessage) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setStatus(AsyncTaskStatusEnum.FAILED.getStatus());
        chart.setGenErrorMessage(errorMessage);
        this.updateById(chart);
    }

}




