package com.zhanghui.constant;

/**
 * @ClassName
 * @Description: 这是描述信息
 * @Author: ZhangHui
 * @Date: 2020/10/27
 * @Version：1.0
 */
public enum RouterStategyEnum {

    SCHEDULER_STRATEGY_RANDOM(0),
    SCHEDULER_STRATEGY_HASH(1),
    SCHEDULER_STRATEGY_LOADFACTOR(2),
    SCHEDULER_STRATEGY_POLLING(3),
    SCHEDULER_STRATEGY_BROADCAST(4),
    SCHEDULER_STRATEGY_SHARDING(5);

    private int status;

    RouterStategyEnum(int status){
        this.status = status;
    }

    public int getStatus(){
        return this.status;
    }
}
