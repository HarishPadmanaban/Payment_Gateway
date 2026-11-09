package com.example.Idempotency.entity;

import java.time.Instant;

public class IdempotencyKey {

    private String key;

    private IdempotencyStatus status;

    private Long merchantId;

    private Instant issuedAt;

    private Instant updatedAt;

    public IdempotencyKey() {
    }

    public IdempotencyKey(String key, Long merchantId) {
        this.key = key;
        this.merchantId = merchantId;
        this.status = IdempotencyStatus.UNUSED;
        this.issuedAt = Instant.now();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public IdempotencyStatus  getStatus() {
        return status;
    }

    public void setStatus(IdempotencyStatus status) {
        this.status = status;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

}
