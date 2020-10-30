package com.zhanghui.core.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @author: ZhangHui
 * @date: 2020/10/28 13:01
 * @version：1.0
 */
@Data
@ToString
public class TesseractAdminRegistryFailInfo {

   private TesseractAdminJobDetailDTO jobDetailDTO;

   private Integer errorCode;

   private String errorMessage;
}
