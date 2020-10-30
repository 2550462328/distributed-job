package com.zhanghui.config;

import com.zhanghui.controller.JobNotifyController;
import com.zhanghui.core.JobClientBootstrap;
import feign.codec.Decoder;
import feign.codec.Encoder;
import com.zhanghui.feignService.IClientFeignService;
import com.zhanghui.postprocessor.JobDetailBeanFactoryPostProcessor;
import feign.Feign;
import feign.Target;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

/**
 * @author: ZhangHui
 * @date: 2020/10/22 14:35
 * @version：1.0
 */
@Configuration
@Import(FeignClientsConfiguration.class)
public class ClientJobConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public JobClientBootstrap jobClientBootstrap(){
        return new JobClientBootstrap();
    }

    @Bean
    public IClientFeignService iClientFeignService() {
         Decoder decoder = (Decoder) applicationContext.getBean("feignDecoder");

         Encoder encoder = (Encoder) applicationContext.getBean("feignEncoder");

        return Feign.builder().encoder(encoder).decoder(decoder)
                .target(Target.EmptyTarget.create(IClientFeignService.class));
    }

//    @Bean
//    public JobNotifyController jobNotifyController(){
//        return new JobNotifyController();
//    }

    @Bean
    public JobDetailBeanFactoryPostProcessor jobDetailBeanFactoryPostProcessor(){
        return new JobDetailBeanFactoryPostProcessor();
    }
}
