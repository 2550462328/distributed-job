package com.zhanghui.core.cache;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.zhanghui.entity.TesseractTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * 基于MongoDB提供对trigger的cache操作
 *
 * @author: ZhangHui
 * @date: 2020/11/2 14:12
 * @version：1.0
 */
public class TriggerMongoCache {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void addTriggerToCache(TesseractTrigger tesseractTrigger) {
        mongoTemplate.insert(tesseractTrigger, tesseractTrigger.getGroupName());
    }

    /**
     * 批量更新
     */
    public void addBatchTriggerToCache(List<TesseractTrigger> tesseractTriggerList, String groupName) {
        tesseractTriggerList.parallelStream().forEach(tesseractTrigger -> {
            Query query = new Query(Criteria.where("id").is(tesseractTrigger.getId()));
            Update update = new Update();
            update.set("prevTriggerTime",tesseractTrigger.getPrevTriggerTime());
            update.set("nextTriggerTime",tesseractTrigger.getNextTriggerTime());
            mongoTemplate.findAndModify(query,update,TesseractTrigger.class,groupName);
        });
    }

    public List<TesseractTrigger> listAllTriggerFromCache(String groupName) {
        return mongoTemplate.findAll(TesseractTrigger.class, groupName);
    }

    public boolean removeTriggerInCache(TesseractTrigger tesseractTrigger) {
        return mongoTemplate.remove(tesseractTrigger, tesseractTrigger.getGroupName()).wasAcknowledged();
    }

    public void removeAllTriggerFromCache(String groupName) {
        mongoTemplate.dropCollection(groupName);
    }

    public List<TesseractTrigger> findByQuery(Query query, String groupName) {
        return mongoTemplate.find(query, TesseractTrigger.class, groupName);
    }


}
