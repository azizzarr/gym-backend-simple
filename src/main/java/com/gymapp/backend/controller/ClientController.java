package com.gymapp.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymapp.backend.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private final ClientService clientService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ClientController(ClientService clientService, ObjectMapper objectMapper) {
        this.clientService = clientService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public Mono<ResponseEntity<JsonNode>> createClient(@RequestBody Object clientData) {
        try {
            String jsonBody = objectMapper.writeValueAsString(clientData);
            logger.info("Received request to create client with body: {}", jsonBody);
            return clientService.createClient(clientData)
                    .map(client -> ResponseEntity.status(201).body(client))
                    .onErrorResume(e -> {
                        logger.error("Error creating client: {}", e.getMessage());
                        return Mono.just(ResponseEntity.badRequest().build());
                    });
        } catch (Exception e) {
            logger.error("Error processing create client request: {}", e.getMessage());
            return Mono.just(ResponseEntity.badRequest().build());
        }
    }

    @GetMapping("/getAllClients")
    public Mono<ResponseEntity<JsonNode>> getAllClients() {
        logger.info("Received request to get all clients");
        return clientService.getAllClients()
                .map(clients -> {
                    logger.info("Successfully retrieved clients");
                    return ResponseEntity.ok(clients);
                })
                .onErrorResume(error -> {
                    logger.error("Error retrieving clients: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(500)
                            .body(objectMapper.createObjectNode()
                                    .put("error", "Failed to retrieve clients: " + error.getMessage())));
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<JsonNode>> getClientById(@PathVariable String id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<JsonNode>> updateClient(@PathVariable String id, @RequestBody Object clientData) {
        return clientService.updateClient(id, clientData)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteClient(@PathVariable String id) {
        return clientService.deleteClient(id)
                .map(result -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
} 