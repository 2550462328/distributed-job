package com.zhanghui.core.cache;

import com.zhanghui.entity.TesseractExecutorDetail;
import com.zhanghui.pojo.ExecutorDetailHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存ExecutorDetail相关信息，并提供对缓存结果的操作
 * @author: ZhangHui
 * @date: 2020/10/26 10:55
 * @version：1.0
 */
public class ExecutorDetailCache {

    private final Map<String, ExecutorDetailHolder> executorDetailMap;

    public ExecutorDetailCache(){
        this.executorDetailMap = new ConcurrentHashMap<>();
    }

    public void addCacheExecutor(String executorName, ExecutorDetailHolder executorDetailHolder){
        this.executorDetailMap.putIfAbsent(executorName, executorDetailHolder);
    }

    public void removeCacheExecutor(String executorName){
        this.executorDetailMap.remove(executorName);
    }

    public ExecutorDetailHolder getCacheExecutor(String executorName) {return this.executorDetailMap.get(executorName);}

    public void clear(){
        this.executorDetailMap.clear();
    }
}
