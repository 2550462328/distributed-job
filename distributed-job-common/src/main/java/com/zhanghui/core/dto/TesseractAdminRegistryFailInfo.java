package com.zhanghui.core.dto;

import lombok.Data;
import lombok.ToString;

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

   public static TesseractAdminRegistryFailInfo build(TesseractAdminJobDetailDTO jobDetailDTO, int errorCode, String errorMessage) {
      TesseractAdminRegistryFailInfo registryFailInfo = new TesseractAdminRegistryFailInfo();
      registryFailInfo.setJobDetailDTO(jobDetailDTO);
      registryFailInfo.setErrorCode(errorCode);
      registryFailInfo.setErrorMessage(errorMessage);
      return registryFailInfo;
   }
}
