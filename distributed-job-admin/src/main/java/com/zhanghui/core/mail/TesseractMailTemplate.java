package com.zhanghui.core.mail;

import com.zhanghui.exception.TesseractException;
import freemarker.template.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.Map;

/**
 * 〈〉
 *
 * @author nickel
 * @create 2019/7/12
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@Slf4j
public class TesseractMailTemplate {
    private Configuration configuration;

    public String buildMailBody(String templateName, Map<String, Object> model) {
        String body;
        try {
            body = FreeMarkerTemplateUtils.processTemplateIntoString(
                    configuration.getTemplate(templateName), model);
        } catch (Exception e) {
            log.error("模板转换异常:{}", e.getMessage());
            throw new TesseractException("模板转换异常");
        }
        return body;
    }
}
