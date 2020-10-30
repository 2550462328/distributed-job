package com.zhanghui.core.handler;

import com.zhanghui.core.handler.context.JobClientContext;

/**
 * @author: ZhangHui
 * @date: 2020/10/22 15:54
 * @version：1.0
 */
public interface JobHandler {
    void execute(JobClientContext clientContext);
}
