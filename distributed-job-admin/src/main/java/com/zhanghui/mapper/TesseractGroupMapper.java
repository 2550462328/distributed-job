package com.zhanghui.mapper;

import com.zhanghui.entity.TesseractGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
public interface TesseractGroupMapper extends BaseMapper<TesseractGroup> {

    @Select("select id ,name from tesseract_group")
    List<TesseractGroup> listAllIdAndName();
}
