package com.zhanghui.config.redis;

import com.zhanghui.core.lock.RedissLockUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass(Config.class)
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonAutoConfiguration {

    @Autowired
    private RedissonProperties redssionProperties;

    /**
     * 装配locker类，并将实例注入到RedissLockUtil中
     */
    @Bean
    RedissLockUtil redissLockUtil(RedissonClient redissonClient) {
        RedissLockUtil redissLockUtil = new RedissLockUtil();
        redissLockUtil.setRedissonClient(redissonClient);
        return redissLockUtil;
    }

    /**
     * 单机模式自动装配
     */
    @Bean
    @ConditionalOnProperty(name = "redisson.address")
    RedissonClient redissonSingle() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(redssionProperties.getAddress())
                .setTimeout(redssionProperties.getTimeout())
                .setConnectionPoolSize(redssionProperties.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(redssionProperties.getConnectionMinimumIdleSize());
        if (!StringUtils.isEmpty(redssionProperties.getPassword())) {
            serverConfig.setPassword(redssionProperties.getPassword());
        }

        return Redisson.create(config);
    }

    /**
     * 哨兵模式自动装配
     */
//    @Bean
//    @ConditionalOnProperty(name="redisson.master-name")
//    RedissonClient redissonSentinel() {
//        Config config = new Config();
//        SentinelServersConfig serverConfig = config.useSentinelServers().addSentinelAddress(redssionProperties.getSentinelAddresses())
//                .setMasterName(redssionProperties.getMasterName())
//                .setTimeout(redssionProperties.getTimeout())
//                .setMasterConnectionPoolSize(redssionProperties.getMasterConnectionPoolSize())
//                .setSlaveConnectionPoolSize(redssionProperties.getSlaveConnectionPoolSize());
//
//        if(StringUtils.isNotBlank(redssionProperties.getPassword())) {
//            serverConfig.setPassword(redssionProperties.getPassword());
//        }
//        return Redisson.create(config);
//    }

    /**
     * 集群模式自动装配
     */
//    @Bean
//    public RedissonClient cluster() {
//        Config config = new Config();
//        config.useClusterServers()
//                .setScanInterval(1000 * 2)
//                .addNodeAddress("redis://10.49.196.20:7000", "redis://10.49.196.20:7001")
//                .addNodeAddress("redis://10.49.196.21:7000", "redis://10.49.196.21:7001")
//                .addNodeAddress("redis://10.49.196.22:7000", "redis://10.49.196.22:7001")
//                .setPassword("123456")
//                .setMasterConnectionPoolSize(5)
//                .setMasterConnectionMinimumIdleSize(2)
//                .setSlaveConnectionPoolSize(5)
//                .setSlaveConnectionMinimumIdleSize(2);
//        RedissonClient client = Redisson.create(config);
//        return client;
//    }

    /**
     * 主从模式
     */
//    @Bean
//    public RedissonClient masterSlave() {
//        Config config = new Config();
//        config.useMasterSlaveServers()
//                .setMasterAddress("redis://10.49.196.20:6379")
//                .addSlaveAddress("redis://10.49.196.21:6379")
//                .addSlaveAddress("redis://10.49.196.22:6379")
//                .setPassword("123456")
//                .setMasterConnectionPoolSize(5)//主节点连接池大小
//                .setMasterConnectionMinimumIdleSize(2)//主节点最小空闲连接数
//                .setSlaveConnectionPoolSize(5)//从节点连接池大小
//                .setSlaveConnectionMinimumIdleSize(2)//从节点最小空闲连接数
//                .setDatabase(0);
//        RedissonClient client = Redisson.create(config);
//        return client;
//    }


}
