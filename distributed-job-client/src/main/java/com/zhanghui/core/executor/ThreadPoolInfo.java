package com.zhanghui.core.executor;

import lombok.Data;

/**
 * 线程池信息
 * @author: ZhangHui
 * @date: 2020/10/27 15:02
 * @version：1.0
 */
@Data
public class ThreadPoolInfo {

    private int activeCount;

    private int  maximumPoolSize;

}
