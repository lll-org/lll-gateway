package top.lll44556.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisSessionConfig {
    // 对于复杂多态对象无法正常序列化
    @Bean
    public RedisSerializationContext.SerializationPair<Object> redisSerializationPair() {
        return RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer());
    }

}
