//package com.alcw.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class LawService {
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    @Value("${groq.api.url}")
//    private String apiUrl;
//
//    @Value("${groq.api.key}")
//    private String apiKey;
//
//    @Value("${groq.model}")
//    private String model;
//
//    @Value("${topic.keywords}")
//    private List<String> topicKeywords;
//
//    public boolean isQuestionValid(String question) {
//        String lowerQuestion = question.toLowerCase();
//        return topicKeywords.stream()
//                .anyMatch(keyword -> lowerQuestion.contains(keyword.toLowerCase()));
//    }
//
//    public String getAIResponse(String question) {
//        // Prepare headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Bearer " + apiKey);
//
//        // Groq-specific request format
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("model", model);
//        requestBody.put("messages", Arrays.asList(
//                Map.of("role", "system",
//                        "content", "You are a legal expert in art/artist law. " +
//                                "Provide concise, accurate answers about copyright, " +
//                                "IP, and legal matters for artists."),
//                Map.of("role", "user", "content", question)
//        ));
//        requestBody.put("temperature", 0.7);
//
//        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//        // Call Groq API
//        ResponseEntity<Map> response = restTemplate.exchange(
//                apiUrl,
//                HttpMethod.POST,
//                requestEntity,
//                Map.class
//        );
//
//        // Parse Groq response
//        Map<String, Object> responseBody = response.getBody();
//        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
//        Map<String, Object> firstChoice = choices.get(0);
//        Map<String, String> message = (Map<String, String>) firstChoice.get("message");
//        return message.get("content");
//    }
//}
