package com.zhanghui.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhanghui.entity.TesseractTrigger;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.toolkit.Constants;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
public interface TesseractTriggerMapper extends BaseMapper<TesseractTrigger> {

    Integer findIfExistsByWrapper(@Param(Constants.WRAPPER) QueryWrapper<TesseractTrigger> wrapper);
}
