package com.sqlengine;

import com.sqlengine.manager.DatabaseConnectionPoolManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseConfigPreloader implements CommandLineRunner {

    private final DatabaseConnectionPoolManager poolManager;

    @Override
    public void run(String... args) {
        log.info("🚀 Starting DatabaseConfigPreloader...");
        poolManager.preloadConnections(200);
    }
}
