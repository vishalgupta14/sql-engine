package com.sqlengine.controller;

import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.service.DatabaseConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/database-config")
@RequiredArgsConstructor
public class DatabaseConfigController {

    private final DatabaseConfigService service;

    @PostMapping
    public Mono<ResponseEntity<DatabaseConfig>> create(@RequestBody DatabaseConfig config) {
        return service.save(config).map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<DatabaseConfig>> getById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientName}")
    public Mono<ResponseEntity<DatabaseConfig>> getByClientName(@PathVariable String clientName) {
        return service.findByClientName(clientName)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<DatabaseConfig> getAll() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<DatabaseConfig>> update(@PathVariable String id, @RequestBody DatabaseConfig updated) {
        return service.update(id, updated).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return service.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
