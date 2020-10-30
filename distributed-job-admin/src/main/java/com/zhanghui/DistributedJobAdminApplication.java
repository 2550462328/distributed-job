package com.zhanghui;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.sql.Timestamp;

/**
 * @author: ZhangHui
 * @date: 2020/10/20 16:18
 * @version：1.0
 */
@SpringBootApplication
@MapperScan("com.zhanghui.mapper")
@EnableFeignClients
public class DistributedJobAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(DistributedJobAdminApplication.class, args);

    }
}
