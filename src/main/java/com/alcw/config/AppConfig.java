package com.alcw.config;

import brevo.ApiClient;
import brevo.auth.ApiKeyAuth;
import brevoApi.TransactionalEmailsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration // Marks this class as a source of bean definitions
public class AppConfig {

    @Bean // Tells Spring to create a bean from the return value of this method
    public RestTemplate restTemplate() {
        // You can customize the RestTemplate here if needed (e.g., timeouts, message converters)
        return new RestTemplate();
    }

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Bean
    public ApiClient brevoApiClient() {
        ApiClient apiClient = new ApiClient();
        // name 'api-key' is the auth name used by the generated SDK - adjust if yours differs
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) apiClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(brevoApiKey);
        return apiClient;
    }

    @Bean
    public TransactionalEmailsApi transactionalEmailsApi(ApiClient brevoApiClient) {
        return new TransactionalEmailsApi(brevoApiClient);
    }
}
