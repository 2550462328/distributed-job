package com.zhanghui.core.registry;

import com.zhanghui.core.dto.TesseractAdminJobDetailDTO;
import com.zhanghui.entity.TesseractGroup;
import com.zhanghui.entity.TesseractTrigger;
import lombok.Data;

/**
 * @author: ZhangHui
 * @date: 2020/11/4 11:34
 * @version：1.0
 */
@Data
public class RegistryTransportDTO {

    private TesseractAdminJobDetailDTO jobDetailDTO;

    private TesseractTrigger tesseractTrigger;

    private TesseractGroup tesseractGroup;
}
