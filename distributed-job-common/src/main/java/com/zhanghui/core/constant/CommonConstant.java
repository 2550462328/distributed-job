package com.zhanghui.core.constant;

import lombok.SneakyThrows;

import java.net.InetAddress;
import java.sql.Timestamp;

public class CommonConstant {
    /**
     * 请求地址
     */
    public static final String EXECUTE_MAPPING = "/execute";
    public static final String REGISTRY_MAPPING_SUFFIX = "/registry";
    public static final String REGISTRY_MAPPING = "/tesseract-job-detail" + REGISTRY_MAPPING_SUFFIX;

    public static final String NOTIFY_MAPPING_SUFFIX = "/notify";
    public static final String NOTIFY_MAPPING = "/tesseract-log" + NOTIFY_MAPPING_SUFFIX;

    public static final String HEARTBEAT_MAPPING_SUFFIX = "/heartBeat";
    public static final String HEARTBEAT_MAPPING = "/tesseract-executor-detail" + HEARTBEAT_MAPPING_SUFFIX;

    /**
     * 状态码
     */
    public static final Integer REGISTRY_REPEAT = 400;
    public static final Integer REGISTRY_INFO_ERROR = 505;
    public static final Integer EXECUTOR_DETAIL_NOT_FIND = 501;
    public static final Integer SERVER_ERROR = 500;


    /**
     * http
     */
    public static final String HTTP_PREFIX = "http://";

    public static final String DEFAULT_CREATER = "admin";
    public static final int DEFAULT_REPEAT_TIMES = 3;
    public static final int DEFAULT_SHARDING_NUM = 1;
    public static final int DEFAULT_THREAD_NUM = 10;


}
