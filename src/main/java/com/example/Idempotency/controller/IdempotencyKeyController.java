package com.example.Idempotency.controller;

import com.example.Idempotency.entity.IdempotencyKey;
import com.example.Idempotency.service.IdempotencyKeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/idempotency-keys")
public class IdempotencyKeyController {

    private final IdempotencyKeyService service;

    public IdempotencyKeyController(IdempotencyKeyService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generate(@RequestParam Long merchantId) {
        String key = service.generateKey(merchantId);
        return ResponseEntity.ok(Map.of("idempotencyKey", key));
    }

    @GetMapping("/{key}")
    public ResponseEntity<IdempotencyKey> get(@PathVariable String key) {

        return service.findByKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{key}/claim")
    public ResponseEntity<String> claim(@PathVariable String key) {
        boolean claimed = service.claimKey(key);

        if (claimed) {
            return ResponseEntity.ok("Idempotency key claimed successfully");
        }

        return ResponseEntity.status(409).body("Idempotency key is already used or does not exist");
    }

    @PostMapping("/{key}/success")
    public ResponseEntity<String> markSuccess(@PathVariable String key) {
        boolean updated = service.markSuccess(key);
        if (updated) {
            return ResponseEntity.ok("Idempotency key marked SUCCESS");
        }
        return ResponseEntity.status(409).body("Idempotency key is not in PROCESSING state");
    }

    @PostMapping("/{key}/failure")
    public ResponseEntity<String> markFailure(@PathVariable String key) {
        boolean updated = service.markFailure(key);
        if (updated) {
            return ResponseEntity.ok("Idempotency key marked FAILURE");
        }
        return ResponseEntity.status(409).body("Idempotency key is not in PROCESSING state");
    }

}