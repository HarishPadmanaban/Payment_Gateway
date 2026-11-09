package com.example.Idempotency.service;

import com.example.Idempotency.entity.IdempotencyKey;
import com.example.Idempotency.repository.IdempotencyKeyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyKeyService {

    private final IdempotencyKeyRepository repository;

    public IdempotencyKeyService(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    public String generateKey(Long merchantId) {
        String key = "idm_" + UUID.randomUUID().toString().replace("-", "");
        IdempotencyKey idempotencyKey = new IdempotencyKey(key, merchantId);
        repository.save(idempotencyKey);
        return key;
    }

    public Optional<IdempotencyKey> findByKey(String key) {
        return repository.findByKey(key);
    }

    public boolean claimKey(String key) {
        return repository.claim(key);
    }

    public boolean markSuccess(String key) {
        return repository.markSuccess(key);
    }

    public boolean markFailure(String key) {
        return repository.markFailure(key);
    }
}