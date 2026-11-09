package com.example.Idempotency;

import com.example.Idempotency.entity.IdempotencyKey;
import com.example.Idempotency.repository.IdempotencyKeyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IdempotencyKeyConcurrencyTest {

    @Autowired
    private IdempotencyKeyRepository repository;

    @Test
    void onlyOneRequestShouldClaimKeyConcurrently()
            throws Exception {

        // 1. Create one idempotency key
        String key = "idm_test_concurrent_001";

        IdempotencyKey idempotencyKey =
                new IdempotencyKey(key, 100L);

        repository.save(idempotencyKey);

        // 2. Number of concurrent requests
        int requestCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(requestCount);

        // Used to make all threads start together
        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<Boolean>> results =
                new ArrayList<>();

        // 3. Prepare concurrent requests
        for (int i = 0; i < requestCount; i++) {

            Future<Boolean> result =
                    executor.submit(() -> {

                        // Wait until all threads are ready
                        startLatch.await();

                        // Try to claim the SAME key
                        return repository.claim(key);
                    });

            results.add(result);
        }

        // 4. Release all threads
        startLatch.countDown();

        // 5. Collect results
        int successCount = 0;
        int failureCount = 0;

        for (Future<Boolean> result : results) {

            if (result.get()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        // 6. Stop executor
        executor.shutdown();

        // 7. Verify result
        assertEquals(
                1,
                successCount,
                "Exactly one request should claim the key"
        );

        assertEquals(
                requestCount - 1,
                failureCount,
                "All other requests should fail"
        );

        // 8. Verify final Redis state
        IdempotencyKey stored =
                repository.findByKey(key)
                        .orElseThrow();

        assertEquals(
                "USED",
                stored.getStatus()
        );

        assertNotNull(
                stored.getUpdatedAt()
        );
    }
}