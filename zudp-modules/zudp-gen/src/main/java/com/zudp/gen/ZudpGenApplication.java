package com.zudp.gen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.zudp.common.security.annotation.EnableCustomConfig;
import com.zudp.common.security.annotation.EnableRyFeignClients;
import com.zudp.common.swagger.annotation.EnableCustomSwagger2;

/**
 * 代码生成
 *
 * @author zudp
 */
@EnableCustomConfig
@EnableCustomSwagger2
@EnableRyFeignClients
@SpringBootApplication
public class ZudpGenApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ZudpGenApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  代码生成模块启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
