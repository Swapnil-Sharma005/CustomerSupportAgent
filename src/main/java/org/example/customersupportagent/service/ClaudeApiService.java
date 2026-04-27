package org.example.customersupportagent.service;

import org.example.customersupportagent.model.Message;
import org.example.customersupportagent.model.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClaudeApiService {

    @Value("${claude.api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, Object> sendMessage(List<Message> messages, List<Tool> tools) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-sonnet-4-20250514");
        requestBody.put("max_tokens", 1000);
        requestBody.put("tools", tools);
        requestBody.put("messages", messages);
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        Map<String, Object> response = restTemplate.postForObject("https://api.anthropic.com/v1/messages", requestEntity, Map.class);
        return response;
    }
}