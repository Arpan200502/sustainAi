package com.example.sustainai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${groq.api-key:}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    @PostMapping
    public Map<String, String> askGroq(@RequestBody AiRequest request) throws Exception {
        if (request.name() == null || request.name().isBlank()
                || request.prompt() == null || request.prompt().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Name and prompt are required.");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Groq API key is not configured.");
        }

        String userMessage = "The user's name is " + request.name().trim()
                + ". Answer the user's request and naturally address them by name. "
                + "User's request: " + request.prompt().trim();

        Map<String, Object> body = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system",
                       "content", "You are a helpful assistant. Be clear and concise."),
                Map.of("role", "user", "content", userMessage)
            ),
            "temperature", 0.7
        );

        String json = mapper.writeValueAsString(body);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = http.send(
                httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Groq API returned HTTP " + response.statusCode()
                    + ". Check your API key, model name, and usage limits.");
        }

        JsonNode root = mapper.readTree(response.body());
        String answer = root.path("choices").path(0)
                .path("message").path("content").asText();

        return Map.of("reply", answer);
    }

    public record AiRequest(String name, String prompt) {}
}
