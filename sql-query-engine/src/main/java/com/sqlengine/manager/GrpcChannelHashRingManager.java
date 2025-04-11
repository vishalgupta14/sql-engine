package com.sqlengine.manager;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sticky, consistent-hashed gRPC channel manager using SHA-256.
 */
@Component
public class GrpcChannelHashRingManager {

    private final List<Integer> ports;
    private final String host;
    private final Map<Integer, ManagedChannel> channelCache = new ConcurrentHashMap<>();
    private final TreeMap<Integer, Integer> consistentHashRing = new TreeMap<>();

    public GrpcChannelHashRingManager(
            @Value("${grpc.execution.ports}") String portsCsv,
            @Value("${grpc.execution.host}") String host
    ) {
        this.host = host;
        this.ports = Arrays.stream(portsCsv.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();

        buildHashRing();
    }

    private void buildHashRing() {
        for (int port : ports) {
            for (int v = 0; v < 100; v++) {
                String virtualNodeId = port + "-VN-" + v;
                int hash = computeHash(virtualNodeId);
                consistentHashRing.put(hash, port);
            }
        }
    }

    public ManagedChannel getChannelForKey(String key) {
        int hash = computeHash(key);
        Map.Entry<Integer, Integer> entry = consistentHashRing.ceilingEntry(hash);
        if (entry == null) {
            entry = consistentHashRing.firstEntry(); // wrap around
        }
        int selectedPort = entry.getValue();
        return channelCache.computeIfAbsent(selectedPort,
                p -> ManagedChannelBuilder.forAddress(host, p).usePlaintext().build());
    }

    /**
     * Generates a stable 32-bit int from the first 8 hex chars of SHA-256 hash.
     */
    private int computeHash(String key) {
        String sha256Hex = DigestUtils.sha256Hex(key);
        return Integer.parseUnsignedInt(sha256Hex.substring(0, 8), 16);
    }
}
