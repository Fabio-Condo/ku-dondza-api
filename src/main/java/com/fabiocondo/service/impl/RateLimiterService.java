package com.fabiocondo.service.impl;

import io.github.bucket4j.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucketForUser(String username) {
        return buckets.computeIfAbsent(username, this::createNewBucket);
    }

    private Bucket createNewBucket(String username) {
        Bandwidth limit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofMinutes(5)));
        return Bucket4j.builder().addLimit(limit).build();
    }
}

