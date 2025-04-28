package com.gymapp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class SupabaseConfig {

    @Bean
    public WebClient supabaseWebClient() {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(30))
            .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);

        return WebClient.builder()
            .baseUrl("https://gspzemcirkfilhudmveu.supabase.co")
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdzcHplbWNpcmtmaWxodWRtdmV1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ4MTE4MTgsImV4cCI6MjA2MDM4NzgxOH0.g7TFJwMuCdpiZDJzR89xX-6mPzvOxxMpo-9YRSKdd4g")
            .defaultHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdzcHplbWNpcmtmaWxodWRtdmV1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ4MTE4MTgsImV4cCI6MjA2MDM4NzgxOH0.g7TFJwMuCdpiZDJzR89xX-6mPzvOxxMpo-9YRSKdd4g")
            .build();
    }
} 