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
@ApiModel(value="TesseractTrigger对象", description="")
public class TesseractTrigger implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private Long nextTriggerTime;

    private Long prevTriggerTime;

    private String cron;

    private Integer strategy;

    private Integer shardingNum;

    private Integer retryCount;

    private Integer status;

    private String creator;

    private String description;

    private Long createTime;

    private Long updateTime;

    private String groupName;

    private Integer groupId;

    private Boolean logFlag;
}
