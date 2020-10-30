package com.zhanghui.core.dto;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class TesseractAdminRegistryResDTO {
    private List<TesseractAdminRegistryFailInfo> notRegistryJobList;
}
