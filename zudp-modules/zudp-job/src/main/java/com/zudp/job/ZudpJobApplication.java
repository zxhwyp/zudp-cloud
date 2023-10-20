package com.zudp.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.zudp.common.security.annotation.EnableCustomConfig;
import com.zudp.common.security.annotation.EnableRyFeignClients;
import com.zudp.common.swagger.annotation.EnableCustomSwagger2;

/**
 * 定时任务
 *
 * @author zudp
 */
@EnableCustomConfig
@EnableCustomSwagger2
@EnableRyFeignClients
@SpringBootApplication
public class ZudpJobApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ZudpJobApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  定时任务模块启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
