package com.triptune.global.redis;

import com.triptune.global.redis.enums.RedisKeyType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class RedisService {

    private final StringRedisTemplate template;

    public boolean existData(RedisKeyType keyType, Object key){
        return Boolean.TRUE.equals(template.hasKey(keyType.generateKey(key)));
    }

    public void save(RedisKeyType keyType, Object key, String value, long ttlSeconds){
        template.opsForValue().set(
                keyType.generateKey(key),
                value,
                Duration.ofSeconds(ttlSeconds)
        );
    }

    public String getData(RedisKeyType keyType, Object key){
        return template.opsForValue().get(keyType.generateKey(key));
    }

    public void deleteData(RedisKeyType keyType, Object key){
        template.delete(keyType.generateKey(key));
    }



}
