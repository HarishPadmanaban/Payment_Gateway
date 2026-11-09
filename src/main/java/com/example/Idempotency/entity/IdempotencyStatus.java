package com.example.Idempotency.entity;

public enum IdempotencyStatus {
    UNUSED,
    PROCESSING,
    SUCCESS,
    FAILURE
}
