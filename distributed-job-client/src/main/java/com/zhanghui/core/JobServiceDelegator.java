package com.zhanghui.core;

import com.zhanghui.core.annotation.ClientJobDetail;
import com.zhanghui.feignService.IClientFeignService;

import java.util.List;

/**
 * @author: ZhangHui
 * @date: 2020/10/27 14:21
 * @version：1.0
 */
public class JobServiceDelegator {

    public static IClientFeignService clientFeignService;

    public static List<ClientJobDetail> clientJobDetailList;

    public static String adminServerAddress;

    public static String serverPort;
}
