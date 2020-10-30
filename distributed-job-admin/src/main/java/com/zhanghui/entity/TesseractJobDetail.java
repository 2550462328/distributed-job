package com.zhanghui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="TesseractJobDetail对象", description="")
@Builder
public class TesseractJobDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer triggerId;

    private String className;

    private Long createTime;

    private String creator;


}
