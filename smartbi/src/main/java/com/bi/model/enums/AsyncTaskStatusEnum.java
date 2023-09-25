package com.bi.model.enums;

/**
 * 异步任务状态
 *
 * @author mendax
 * @version 2023/9/25 17:14
 */
public enum AsyncTaskStatusEnum {

    /**
     * 执行状态记录
     */
    SUCCESS(1, "成功"),
    RUNNING(2, "正在执行"),
    FAILED(0, "失败");

    private final Integer status;
    private final String text;

    AsyncTaskStatusEnum(Integer status, String text) {
        this.status = status;
        this.text = text;
    }


    public Integer getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }
}
