package com.zhanghui;

import com.zhanghui.config.ClientJobConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.sql.Timestamp;

@SpringBootApplication
@Import(ClientJobConfig.class)
public class DistributedJobSampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(DistributedJobSampleApplication.class, args);
    }
}




