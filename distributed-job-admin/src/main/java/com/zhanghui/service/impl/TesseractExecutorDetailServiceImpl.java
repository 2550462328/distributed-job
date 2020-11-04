package com.zhanghui.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zhanghui.core.dto.TesseractHeartbeatRequest;
import com.zhanghui.core.JobServerBootstrap;
import com.zhanghui.entity.TesseractExecutorDetail;
import com.zhanghui.entity.TesseractGroup;
import com.zhanghui.mapper.TesseractExecutorDetailMapper;
import com.zhanghui.pojo.ExecutorDetailHolder;
import com.zhanghui.service.ITesseractExecutorDetailService;
import com.zhanghui.service.ITesseractGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
@Slf4j
@Service
public class TesseractExecutorDetailServiceImpl extends ServiceImpl<TesseractExecutorDetailMapper, TesseractExecutorDetail> implements ITesseractExecutorDetailService {

    @Autowired
    private JobServerBootstrap jobServerBootstrap;

    @Autowired
    private ITesseractGroupService groupService;

    private final int invalidTime = 15 * 1000;

    private final static double MIN_LOADFACTOR = 0.1;

    private final static double MAX_LOADFACTOR = 0.9;

    @Override
    public List<TesseractExecutorDetail> listInvalid() {
        long currentTimeMillis = System.currentTimeMillis();
        QueryWrapper<TesseractExecutorDetail> detailQueryWrapper = new QueryWrapper<>();
        detailQueryWrapper.lambda().le(TesseractExecutorDetail::getUpdateTime, currentTimeMillis - invalidTime);
        return list(detailQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void heartBeat(TesseractHeartbeatRequest heartBeatRequest) {
        String socket = heartBeatRequest.getSocket();
        // 从缓存中拿
        ExecutorDetailHolder cacheExecutor = jobServerBootstrap.getExecutorDetailCache().getCacheExecutor(socket);

        ExecutorDetailHolder executorDetailHolder = new ExecutorDetailHolder(calculateLoadFactor(heartBeatRequest));

        // 缓存中有的情况下 进行批量更新
        if (cacheExecutor != null) {
            QueryWrapper<TesseractExecutorDetail> executorDetailQueryWrapper = new QueryWrapper<>();
            executorDetailQueryWrapper.lambda().eq(TesseractExecutorDetail::getSocket, socket);
            List<TesseractExecutorDetail> executorDetailList = list(executorDetailQueryWrapper);

            if (CollectionUtils.isEmpty(executorDetailList)) {
                return;
            }

            // 修改executorDetail状态
            executorDetailList.forEach(executorDetail -> {
                executorDetail.setLoadFactor(executorDetailHolder.get());
                executorDetail.setUpdateTime(System.currentTimeMillis());
            });

            this.updateBatchById(executorDetailList);
        } else { // 进行批量插入
            List<TesseractExecutorDetail> executorDetailList = batchCreateExecutorDetail(heartBeatRequest.getClientJobGroups(), executorDetailHolder, socket);
            if (this.saveBatch(executorDetailList)) {
                // 插入缓存
                jobServerBootstrap.getExecutorDetailCache().addCacheExecutor(socket, executorDetailHolder);
            }
        }
    }


    private List<TesseractExecutorDetail> batchCreateExecutorDetail(Set<String> clientJobGroups, ExecutorDetailHolder executorDetailHolder, String socket) {

        List<TesseractExecutorDetail> executorDetailList = Collections.synchronizedList(Lists.newArrayList());

        clientJobGroups.parallelStream().forEach(groupName -> {

            QueryWrapper<TesseractGroup> groupQueryWrapper = new QueryWrapper<>();
            groupQueryWrapper.lambda().eq(TesseractGroup::getName, groupName);
            TesseractGroup group = groupService.getOne(groupQueryWrapper);
            if (group == null) {
                log.error("心跳注册失败，未识别的组名：[{}]", groupName);
                return;
            }

            // 新增executorDetail
            TesseractExecutorDetail executorDetail = new TesseractExecutorDetail();
            executorDetail.setLoadFactor(executorDetailHolder.get());
            executorDetail.setCreateTime(System.currentTimeMillis());
            executorDetail.setUpdateTime(System.currentTimeMillis());
            executorDetail.setGroupName(groupName);
            executorDetail.setGroupId(group.getId());
            executorDetail.setSocket(socket);

            executorDetailList.add(executorDetail);
        });

        return executorDetailList;
    }

    /**
     * 根据client端处理Job任务的线程池状态来计算负载因子
     */
    private double calculateLoadFactor(TesseractHeartbeatRequest heartBeatRequest) {
        double poolMessaure = Double.valueOf(heartBeatRequest.getActiveCount()) / heartBeatRequest.getMaximumPoolSize();
        if (poolMessaure == 0) {
            return MIN_LOADFACTOR;
        } else if (poolMessaure == 1) {
            return MAX_LOADFACTOR;
        } else {
            // 保留一位小数
            return new BigDecimal(poolMessaure).setScale(1, BigDecimal.ROUND_CEILING).doubleValue();
        }
    }

}
