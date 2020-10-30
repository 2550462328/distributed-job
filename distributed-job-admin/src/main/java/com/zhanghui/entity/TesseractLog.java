package com.zhanghui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value="TesseractLog对象", description="")
public class TesseractLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    private String triggerName;

    private String className;

    private String groupName;

    private Integer groupId;

    private String socket;

    private Integer status;

    private String msg;

    private String creator;

    private Long createTime;

    private Long endTime;
}
