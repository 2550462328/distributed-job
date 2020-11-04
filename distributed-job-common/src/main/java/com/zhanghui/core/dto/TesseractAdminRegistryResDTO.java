package com.zhanghui.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TesseractAdminRegistryResDTO {
    private List<TesseractAdminRegistryFailInfo> notRegistryJobList;


}
