package com.zhanghui.core.event;

import lombok.Data;

/**
 * 〈发送邮件事件〉
 *
 * @author nickel
 * @create 2019/7/12
 * @since 1.0.0
 */
@Data
public class MailEvent {
    private String to;
    private String subject;
    private String body;
}
