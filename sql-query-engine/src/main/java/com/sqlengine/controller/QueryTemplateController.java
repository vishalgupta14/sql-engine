package com.sqlengine.controller;

import com.sqlengine.model.QueryTemplate;
import com.sqlengine.service.QueryTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/query-template")
@RequiredArgsConstructor
public class QueryTemplateController {

    private final QueryTemplateService service;

    @PostMapping
    public Mono<ResponseEntity<QueryTemplate>> create(@RequestBody QueryTemplate template) {
        return service.save(template)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<QueryTemplate>> getById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{templateName}")
    public Mono<ResponseEntity<QueryTemplate>> getByName(@PathVariable String templateName) {
        return service.findByTemplateName(templateName)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<QueryTemplate> getAll() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<QueryTemplate>> update(@PathVariable String id, @RequestBody QueryTemplate template) {
        return service.update(id, template)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return service.deleteById(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}

