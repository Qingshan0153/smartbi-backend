package com.bi.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mendax
 * @version 2023/9/23 15:48
 */


@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        String goal="分析网站用户的增长情况";
        String data="日期，用户数\n" +
                "1号，10\n" +
                "2号，20\n" +
                "3号，30";
        String chartType="雷达图";
        String answer = aiManager.doChat(goal,data,chartType);

        System.out.println(answer);

    }
}