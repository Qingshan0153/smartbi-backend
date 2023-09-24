package com.bi.manager;

import cn.hutool.core.util.StrUtil;
import com.bi.client.XfXhStreamClient;
import com.bi.config.XfXhConfig;
import com.bi.listener.XfXhWebSocketListener;
import com.bi.model.dto.xfxh.MsgDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.UUID;

/**
 * @author mendax
 * @version 2023/9/23 15:43
 */


@Component
@Slf4j
public class AiManager {

    @Resource
    private XfXhStreamClient xfXhStreamClient;

    @Resource
    private XfXhConfig xfXhConfig;


    /**
     * AI 对话
     * @param goal 分析目标
     * @param data csv 数据集
     * @param chartType 图表类型
     * @return  ai对话内容
     */
    public String doChat(String goal, String data, String chartType) {
        String question = this.splicing(goal, data, chartType);
        // 如果是无效字符串，则不对大模型进行请求
        if (StrUtil.isBlank(question)) {
            return "无效问题，请重新输入";
        }
        // 获取连接令牌
        if (!xfXhStreamClient.operateToken(XfXhStreamClient.GET_TOKEN_STATUS)) {
            return "当前大模型连接数过多，请稍后再试";
        }

        // 创建消息对象
        MsgDTO msgDTO = MsgDTO.createUserMsg(question);
        // 创建监听器
        XfXhWebSocketListener listener = new XfXhWebSocketListener();
        // 发送问题给大模型，生成 websocket 连接
        WebSocket webSocket = xfXhStreamClient.sendMsg(UUID.randomUUID().toString().substring(0, 10), Collections.singletonList(msgDTO), listener);
        if (webSocket == null) {
            // 归还令牌
            xfXhStreamClient.operateToken(XfXhStreamClient.BACK_TOKEN_STATUS);
            return "系统内部错误，请联系管理员";
        }
        try {
            int count = 0;
            // 为了避免死循环，设置循环次数来定义超时时长
            int maxCount = xfXhConfig.getMaxResponseTime() * 5;
            while (count <= maxCount) {
                Thread.sleep(200);
                if (listener.isWsCloseFlag()) {
                    break;
                }
                count++;
            }
            if (count > maxCount) {
                return "大模型响应超时，请联系管理员";
            }
            // 响应大模型的答案
            return listener.getAnswer().toString();
        } catch (InterruptedException e) {
            log.error("错误：" + e.getMessage());
            return "系统内部错误，请联系管理员";
        } finally {
            // 关闭 websocket 连接
            webSocket.close(1000, "");
            // 归还令牌
            xfXhStreamClient.operateToken(XfXhStreamClient.BACK_TOKEN_STATUS);
        }
    }

    private String splicing(String goal, String data, String chartType) {
        StringBuilder content = new StringBuilder();
        /*
        你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：
    分析需求：
    分析网站数据增长情况
    原始数据：
    1号，30 ，2号 60，3号 80 4号 0，5号 30
    请根据分析需求和原始数据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）:
    【【【【【
    生成前端 Echarts V5 的 option 配置对象的 js 代码，不要生成任何多余的内容，比如注释 }
    】】】】】
    生成明确的数据分析结论，越详细越好，不要生成多余的注释
         */
        content.append("你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：").append("\n");
        content.append("分析需求：").append(goal).append("\n");
        content.append("原始数据：").append(data).append("\n");
        content.append("请根据分析需求和原始数据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）").append("\n");
        content.append("【【【【【").append("\n");
        content.append("生成前端可直接执行的 Echarts V5 的").append(chartType).append("option 配置对象的 json 代码，不要生成任何多余的内容，比如注释").append("\n");
        content.append("【【【【【").append("\n");
        content.append("生成明确的数据分析结论内容信息，越详细越好，不要生成多余的注释").append("\n");

        return content.toString();
    }
}
