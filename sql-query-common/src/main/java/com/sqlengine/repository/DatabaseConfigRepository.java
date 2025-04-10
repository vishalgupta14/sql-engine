package com.sqlengine.repository;

import com.sqlengine.model.DatabaseConfig;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface DatabaseConfigRepository extends ReactiveMongoRepository<DatabaseConfig, String> {
    Mono<DatabaseConfig> findByDatabaseConnectionName(String clientName);
}
