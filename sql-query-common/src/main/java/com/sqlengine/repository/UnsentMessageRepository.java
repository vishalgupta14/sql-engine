package com.sqlengine.repository;

import com.sqlengine.model.UnsentMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UnsentMessageRepository extends ReactiveMongoRepository<UnsentMessage, String> {
}