package com.triptune.global.redis;

import com.triptune.global.redis.eums.RedisKeyType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class RedisService {

    private final StringRedisTemplate template;

    public boolean existEmailData(RedisKeyType keyType, String email){
        String key = createEmailKey(keyType, email);
        return existData(key);
    }

    public boolean existData(String key){
        return Boolean.TRUE.equals(template.hasKey(key));
    }

    public void deleteEmailData(RedisKeyType keyType, String email){
        String key = createEmailKey(keyType, email);
        deleteData(key);
    }

    public void deleteData(String key){
        template.delete(key);
    }

    public void saveEmailData(RedisKeyType keyType, String email, String value, long duration){
        String key = createEmailKey(keyType, email);
        saveExpiredData(key, value, duration);
    }

    public void saveExpiredData(String key, String value, long duration){
        ValueOperations<String, String> valueOperations = template.opsForValue();
        Duration expireDuration  = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    public String getEmailData(RedisKeyType keyType, String email){
        String key = createEmailKey(keyType, email);
        return getData(key);
    }

    public String getData(String key){
        ValueOperations<String, String> valueOperations = template.opsForValue();
        return valueOperations.get(key);
    }

    public String createEmailKey(RedisKeyType keyType, String email){
        return String.format("email:%s:%s", email, keyType.getKeyType());
    }


}
