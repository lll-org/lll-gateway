package top.lll44556.gulimall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

@SpringBootApplication
@EnableRedisWebSession
public class LLLGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LLLGatewayApplication.class, args);
    }

}
