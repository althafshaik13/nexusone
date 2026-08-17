package dev.nexusone.ai_copilot_service.client;

import dev.nexusone.ai_copilot_service.exception.AiGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

@Component
public class OllamaClient {

    private final RestClient restClient;
    private final String model;

    public OllamaClient(RestClient.Builder builder,
                         @Value("${nexusone.ai.ollama.base-url}") String baseUrl,
                         @Value("${nexusone.ai.ollama.model}") String model,
                         @Value("${nexusone.ai.ollama.read-timeout-seconds}") int readTimeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.model = model;
    }

    public String generate(String systemPrompt, String userPrompt) {
        ChatRequest request = new ChatRequest(model, List.of(
                new Message("system", systemPrompt),
                new Message("user", userPrompt)
        ), false);
        try {
            ChatResponse response = restClient.post()
                    .uri("/api/chat")
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);
            if (response == null || response.message() == null || response.message().content() == null
                    || response.message().content().isBlank()) {
                throw new AiGenerationException("Ollama returned an empty response");
            }
            return response.message().content();
        } catch (RestClientException e) {
            throw new AiGenerationException("Failed to generate AI content via Ollama: " + e.getMessage());
        }
    }

    private record ChatRequest(String model, List<Message> messages, boolean stream) {
    }

    private record Message(String role, String content) {
    }

    private record ChatResponse(String model, Message message, boolean done) {
    }
}
