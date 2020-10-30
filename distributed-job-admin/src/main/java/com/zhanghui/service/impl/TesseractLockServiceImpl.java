package com.zhanghui.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhanghui.entity.TesseractLock;
import com.zhanghui.mapper.TesseractLockMapper;
import com.zhanghui.service.ITesseractLockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
@Service
public class TesseractLockServiceImpl extends ServiceImpl<TesseractLockMapper, TesseractLock> implements ITesseractLockService {

    private Map<String,Boolean> checkMap = new ConcurrentHashMap<>();

    @Override
    public TesseractLock lock(String lockName, String groupName) {
        String key = lockName + groupName;

        if(!checkMap.containsKey(key)){
            QueryWrapper<TesseractLock> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TesseractLock::getGroupName,groupName).eq(TesseractLock::getName,lockName);
            TesseractLock tesseractLock = getOne(queryWrapper);
            if(tesseractLock == null){
                tesseractLock = new TesseractLock();
                tesseractLock.setGroupName(groupName);
                tesseractLock.setName(lockName);
                this.save(tesseractLock);
                checkMap.put(key,true);
            }
            checkMap.put(key,true);
        }
        return this.baseMapper.lock(lockName,groupName);
    }
}
