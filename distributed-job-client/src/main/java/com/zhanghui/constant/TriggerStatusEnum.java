package com.zhanghui.constant;

/**
 * @ClassName
 * @Description: 这是描述信息
 * @Author: ZhangHui
 * @Date: 2020/10/27
 * @Version：1.0
 */
public enum TriggerStatusEnum {

    TRGGER_STATUS_STOPING(0),
    TRGGER_STATUS_STARTING(1);

    private int status;

    TriggerStatusEnum(int status){
        this.status = status;
    }

    public int getStatus(){
        return this.status;
    }
}
