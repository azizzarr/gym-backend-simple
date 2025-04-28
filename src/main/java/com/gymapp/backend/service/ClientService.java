package com.gymapp.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public ClientService(WebClient supabaseWebClient, ObjectMapper objectMapper) {
        this.webClient = supabaseWebClient;
        this.objectMapper = objectMapper;
    }
    
    public Mono<JsonNode> getAllClients() {
        logger.info("Fetching all clients from Supabase");
        return webClient.get()
            .uri("/rest/v1/clients?select=*")
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        logger.error("Error from Supabase: {}", errorBody);
                        return Mono.error(new RuntimeException("Error fetching clients: " + errorBody));
                    }))
            .bodyToMono(JsonNode.class)
            .doOnSuccess(result -> logger.info("Successfully fetched clients"))
            .doOnError(error -> logger.error("Error fetching clients: {}", error.getMessage()));
    }
    
    public Mono<JsonNode> getClientById(String id) {
        return webClient.get()
            .uri("/rest/v1/clients?id=eq.{id}&select=*", id)
            .retrieve()
            .bodyToMono(JsonNode.class);
    }
    
    public Mono<JsonNode> createClient(Object clientData) {
        try {
            String jsonBody = objectMapper.writeValueAsString(clientData);
            logger.info("Sending request to Supabase with body: {}", jsonBody);
            
            return webClient.post()
                .uri("/rest/v1/clients")
                .bodyValue(clientData)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            logger.error("Error from Supabase: {}", errorBody);
                            return Mono.error(new RuntimeException("Error creating client: " + errorBody));
                        }))
                .bodyToMono(JsonNode.class);
        } catch (Exception e) {
            logger.error("Error creating client: {}", e.getMessage());
            return Mono.error(e);
        }
    }
    
    public Mono<JsonNode> updateClient(String id, Object clientData) {
        return webClient.patch()
            .uri("/rest/v1/clients?id=eq.{id}", id)
            .bodyValue(clientData)
            .retrieve()
            .bodyToMono(JsonNode.class);
    }
    
    public Mono<JsonNode> deleteClient(String id) {
        return webClient.delete()
            .uri("/rest/v1/clients?id=eq.{id}", id)
            .retrieve()
            .bodyToMono(JsonNode.class);
    }
} 