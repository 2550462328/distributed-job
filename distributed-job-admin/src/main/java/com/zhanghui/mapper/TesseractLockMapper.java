package com.zhanghui.mapper;

import com.zhanghui.entity.TesseractLock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
public interface TesseractLockMapper extends BaseMapper<TesseractLock> {
    TesseractLock lock(@Param("groupName") String groupName, @Param("lockName") String lockName);
}
