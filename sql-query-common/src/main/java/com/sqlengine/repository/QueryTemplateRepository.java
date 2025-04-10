package com.sqlengine.repository;

import com.sqlengine.model.QueryTemplate;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface
QueryTemplateRepository extends ReactiveMongoRepository<QueryTemplate, String> {
    Mono<QueryTemplate> findByTemplateName(String templateName);
}
