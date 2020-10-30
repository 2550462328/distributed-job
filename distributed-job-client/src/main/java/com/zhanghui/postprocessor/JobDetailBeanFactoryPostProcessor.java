package com.zhanghui.postprocessor;

import com.zhanghui.core.annotation.ClientJobDetail;
import com.zhanghui.core.annotation.SchedulerJob;
import com.zhanghui.core.handler.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 在Spring的bean实例化前将ClientJobDetail注入进去
 *
 * @author: ZhangHui
 * @date: 2020/10/22 15:38
 * @version：1.0
 */
@Slf4j
public class JobDetailBeanFactoryPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {
    private final static AtomicInteger BEAN_IDX = new AtomicInteger(0);

    public static final String NAME_FORMATTER = "clientJobDetail-%d";

    private String applicationName;

    @Override
    public void setEnvironment(Environment environment) {
        applicationName = environment.getProperty("spring.application.name");
        if (StringUtils.isEmpty(applicationName)) {
            //todo 这里怎么在没有自定义groupName和applicationName情况下生成一个唯一的group
            // 目前只在环境变量中找到这个东西跟客户端有关 最后得到的applicationName就是SampleApplication
            String[] commads = environment.getProperty("sun.java.command").split("\\.");
            applicationName = commads[commads.length - 1];
        }
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();

        Arrays.asList(beanDefinitionNames).parallelStream().forEach(beanName -> {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

            String beanClassName = beanDefinition.getBeanClassName();

            if (StringUtils.isEmpty(beanClassName)) {
                return;
            }
            try {
                Class<?> clazz = Class.forName(beanClassName);
                SchedulerJob schedulerJob;
                if (clazz != null && (schedulerJob = clazz.getAnnotation(SchedulerJob.class)) != null) {
                    if (!JobHandler.class.isAssignableFrom(clazz)) {
                        log.error("SchedulerJob注解必须加在JobHanlder类上，{}不符合要求", clazz.getCanonicalName());
                        return;
                    }
                    ClientJobDetail clientJobDetail = ClientJobDetail.builder()
                            .className(beanClassName)
                            .triggerName(schedulerJob.triggerName())
                            .groupName(StringUtils.isEmpty(schedulerJob.groupName()) ? applicationName : schedulerJob.groupName())
                            .cron(schedulerJob.cron())
                            .desc(schedulerJob.desc())
                            .retryTimes(schedulerJob.retryTimes())
                            .shardingNum(schedulerJob.shardingNum())
                            .isLog(schedulerJob.isLog())
                            .strategy(schedulerJob.strategy().getStatus())
                            .status(schedulerJob.status().getStatus())
                            .build();

                    beanFactory.registerSingleton(NAME_FORMATTER + BEAN_IDX.incrementAndGet(), clientJobDetail);
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        });
    }
}
