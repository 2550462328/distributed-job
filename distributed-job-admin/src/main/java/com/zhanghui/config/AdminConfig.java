package com.zhanghui.config;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zhanghui.core.JobServerBootstrap;
import com.zhanghui.core.cache.TriggerMongoCache;
import com.zhanghui.core.cache.TriggerRedisCache;
import com.zhanghui.core.listener.MailListener;
import com.zhanghui.core.mail.TesseractMailTemplate;
import feignService.IAdminFeignService;
import feign.Feign;
import feign.Request;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.util.concurrent.*;

/**
 * @author: ZhangHui
 * @date: 2020/10/20 16:47
 * @version：1.0
 */
@Configuration
@EnableFeignClients
@Import(FeignClientsConfiguration.class)
public class AdminConfig {

    @Autowired
    private Decoder decoder;

    @Autowired
    private Encoder encoder;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private JavaMailSender mailSender;

    @Bean(initMethod = "init",destroyMethod = "destroy")
    public JobServerBootstrap jobScheduleBootstrap(){
        return new JobServerBootstrap();
    }

    /**
     * 配置feign服务
     *
     * @return
     */
    @Bean
    public IAdminFeignService iAdminFeignService() {
        Request.Options options = new Request.Options(3 * 1000, 3 * 1000, true);
        IAdminFeignService iAdminFeignService = Feign.builder().encoder(encoder).decoder(decoder).options(options)
                .target(Target.EmptyTarget.create(IAdminFeignService.class));
        return iAdminFeignService;
    }

    @Bean
    public TriggerMongoCache triggerMongoCache(){
        return new TriggerMongoCache();
    }

    @Bean
    public TriggerRedisCache triggerRedisCache(){
        return new TriggerRedisCache();
    }

    /**
     * 分页插件
     */
    public InnerInterceptor paginationInterceptor(){
        return new PaginationInnerInterceptor();
    }

    /**
     * mailEventBus
     *
     * @return
     */
    @Bean
    public EventBus mailEventBus() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("mailEventBus-pool-%d").build();
        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(5, 10,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        AsyncEventBus asyncEventBus = new AsyncEventBus("mailEventBus", threadPoolExecutor);
        asyncEventBus.register(new MailListener(mailSender, from));
        return asyncEventBus;
    }

    @Bean
    public TesseractMailTemplate tesseractMailTemplate(@Qualifier("tesseractConfiguration") freemarker.template.Configuration configuration) throws Exception {
        return new TesseractMailTemplate(configuration);
    }

    @Bean("tesseractConfiguration")
    public FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean() {
        FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean = new FreeMarkerConfigurationFactoryBean();
        freeMarkerConfigurationFactoryBean.setTemplateLoaderPath("classpath:mailTemplate");
        return freeMarkerConfigurationFactoryBean;
    }
}
