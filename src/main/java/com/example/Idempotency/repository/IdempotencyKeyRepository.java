package com.example.Idempotency.repository;

import com.example.Idempotency.entity.IdempotencyKey;
import com.example.Idempotency.entity.IdempotencyStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class IdempotencyKeyRepository {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, IdempotencyKey> redisTemplate;

    public IdempotencyKeyRepository(RedisTemplate<String, IdempotencyKey> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(IdempotencyKey idempotencyKey) {
        String redisKey = KEY_PREFIX + idempotencyKey.getKey();
        Map<String, String> fields = Map.of("key", idempotencyKey.getKey(), "status", idempotencyKey.getStatus().name(), "merchantId", String.valueOf(idempotencyKey.getMerchantId()), "issuedAt", idempotencyKey.getIssuedAt().toString());
        redisTemplate.opsForHash().putAll(redisKey, fields);
        redisTemplate.expire(redisKey, TTL);
    }

    public Optional<IdempotencyKey> findByKey(String key) {
        String redisKey = KEY_PREFIX + key;
        Map<Object, Object> fields = redisTemplate.opsForHash().entries(redisKey);
        if (fields.isEmpty()) {
            return Optional.empty();
        }
        IdempotencyKey idempotencyKey = new IdempotencyKey();

        idempotencyKey.setKey((String) fields.get("key"));

        idempotencyKey.setStatus(IdempotencyStatus.valueOf((String) fields.get("status")));

        idempotencyKey.setMerchantId(Long.valueOf((String) fields.get("merchantId")));

        idempotencyKey.setIssuedAt(Instant.parse((String) fields.get("issuedAt")));

        Object updatedAt = fields.get("updatedAt");
        if (updatedAt != null) {
            idempotencyKey.setUpdatedAt(Instant.parse((String) updatedAt));
        }

        return Optional.of(idempotencyKey);
    }

    private boolean transition(String key, IdempotencyStatus expected, IdempotencyStatus next) {
        String redisKey = KEY_PREFIX + key;

        String luaScript = """
                local status = redis.call('HGET', KEYS[1], 'status')
                
                if not status then
                    return 0
                end
                
                if status == ARGV[1] then
                    redis.call('HSET', KEYS[1], 'status', ARGV[2])
                    redis.call('HSET', KEYS[1], 'updatedAt', ARGV[3])
                    return 1
                end
                
                return 0
                """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);

        Long result = redisTemplate.execute(script, List.of(redisKey), expected.name(), next.name(), Instant.now().toString());

        return result != null && result == 1;
    }

    public boolean claim(String key) {
        return transition(key, IdempotencyStatus.UNUSED, IdempotencyStatus.PROCESSING);
    }

    public boolean markSuccess(String key) {
        return transition(key, IdempotencyStatus.PROCESSING, IdempotencyStatus.SUCCESS);
    }

    public boolean markFailure(String key) {
        return transition(key, IdempotencyStatus.PROCESSING, IdempotencyStatus.FAILURE);
    }
}