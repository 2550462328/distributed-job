package com.zhanghui.core.cache;

import com.google.common.collect.Lists;
import com.zhanghui.entity.TesseractTrigger;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 基于Redis提供对trigger的cache操作
 *
 * Deprecated 用不到
 * @author: ZhangHui
 * @date: 2020/10/29 11:53
 * @version：1.0
 */
@Deprecated
public class TriggerRedisCache {

    @Autowired
    private RedissonClient redissonClient;

    private static final String TRIGGER_LIST_NAME_PREFIX = "jobAdminGroup-";

    public void addTriggerToCache(TesseractTrigger tesseractTrigger){
        String keyName = TRIGGER_LIST_NAME_PREFIX + tesseractTrigger.getGroupName();
        RList<TesseractTrigger> rList =  redissonClient.getList(keyName);
        if(rList.isExists()) {
            rList.add(tesseractTrigger);
        }else{
            RBucket<List<TesseractTrigger>> rBucket = redissonClient.getBucket(keyName);
            List newRList = Lists.newArrayList();
            newRList.add(tesseractTrigger);
            rBucket.set(newRList);
        }
    }

    public List<TesseractTrigger> listAllTriggerFromCache(String groupName){
        String keyName = TRIGGER_LIST_NAME_PREFIX + groupName;
        RList<TesseractTrigger> rList =  redissonClient.getList(keyName);
        if(rList.isExists()) {
            return rList.readAll();
        }else{
            return Lists.newArrayList();
        }
    }

    public boolean removeTriggerInCache(TesseractTrigger tesseractTrigger){
        String keyName = TRIGGER_LIST_NAME_PREFIX + tesseractTrigger.getGroupName();
        RList<TesseractTrigger> rList =  redissonClient.getList(keyName);
        if(rList.isExists()) {
            return rList.remove(tesseractTrigger);
        }
        return true;
    }

    public boolean removeAllTriggerFromCache(String groupName){
        String keyName = TRIGGER_LIST_NAME_PREFIX + groupName;
        RBucket rBucket = redissonClient.getBucket(keyName);
        if(rBucket.isExists()) {
            return rBucket.delete();
        }
        return true;
    }
}
