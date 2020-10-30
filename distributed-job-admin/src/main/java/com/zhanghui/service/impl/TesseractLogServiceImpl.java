package com.zhanghui.service.impl;

import com.zhanghui.core.dto.TesseractAdminJobNotify;
import com.zhanghui.entity.TesseractLog;
import com.zhanghui.mapper.TesseractLogMapper;
import com.zhanghui.service.ITesseractLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import static com.zhanghui.constant.AdminConstant.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
@Service
public class TesseractLogServiceImpl extends ServiceImpl<TesseractLogMapper, TesseractLog> implements ITesseractLogService {
    @Override
    public void notify(TesseractAdminJobNotify tesseractAdminJobNotify) {

        TesseractLog tesseractLog = getById(tesseractAdminJobNotify.getLogId());

        if(tesseractLog == null){
            // 无效日志
            return;
        }

        if(StringUtils.isEmpty(tesseractAdminJobNotify.getException())){
            tesseractLog.setStatus(LOG_SUCCESS);
            tesseractLog.setMsg("执行成功");
        }else{
            tesseractLog.setStatus(LOG_FAIL);
            tesseractLog.setMsg(tesseractAdminJobNotify.getException());
        }
        tesseractLog.setEndTime(System.currentTimeMillis());
        this.updateById(tesseractLog);
    }
}
