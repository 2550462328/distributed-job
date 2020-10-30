package com.zhanghui.core.mail;

import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @projectName: tesseract-job-admin
 * @className: SendMailComponent
 * @description: 邮件发送组件，所有的发送邮件的动作都在这里面执行
 * @author: liangxuekai
 * @createDate: 2019-07-23 14:51
 * @updateUser: liangxuekai
 * @updateDate: 2019-07-24 15:51
 * @updateRemark: 添加任务重试失败邮件组件
 * @version: 1.0
 */
@Component
public class TesseractMailSender {

    @Autowired
    private EventBus mailEventBus;

    @Autowired
    private TesseractMailTemplate mailTemplate;

    // todo 邮件提醒相关功能实现


}
