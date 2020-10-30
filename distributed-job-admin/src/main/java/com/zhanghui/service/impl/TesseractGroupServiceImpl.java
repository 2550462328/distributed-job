package com.zhanghui.service.impl;

import com.zhanghui.entity.TesseractGroup;
import com.zhanghui.mapper.TesseractGroupMapper;
import com.zhanghui.service.ITesseractGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
@Service
public class TesseractGroupServiceImpl extends ServiceImpl<TesseractGroupMapper, TesseractGroup> implements ITesseractGroupService {

    @Override
    public List<TesseractGroup> listAllIdAndName(){
        return this.baseMapper.listAllIdAndName();
    }
}
