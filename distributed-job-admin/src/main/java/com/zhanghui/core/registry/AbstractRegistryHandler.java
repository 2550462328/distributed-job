package com.zhanghui.core.registry;

import com.zhanghui.core.constant.OperateResult;
import com.zhanghui.core.dto.TesseractAdminRegistryFailInfo;

/**
 * @author: ZhangHui
 * @date: 2020/11/4 11:19
 * @version：1.0
 */
public abstract class AbstractRegistryHandler {

    public abstract OperateResult<TesseractAdminRegistryFailInfo> handler(RegistryTransportDTO registryTransportDTO);

    private AbstractRegistryHandler successor = null;

    public void setSuccessor(AbstractRegistryHandler successor) {
        this.successor = successor;
    }

    public AbstractRegistryHandler getSuccessor() {
        return successor;
    }
}
