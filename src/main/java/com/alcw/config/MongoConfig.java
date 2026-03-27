package com.alcw.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

// Add this config class
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database:alc}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        String normalized = databaseName == null ? "alc" : databaseName.trim();
        if (normalized.isEmpty() || normalized.contains(" ")) {
            log.warn("Invalid spring.data.mongodb.database='{}'; falling back to 'alc'", databaseName);
            return "alc";
        }
        return normalized;
    }

    @Override
    public MongoClient mongoClient() {
        String normalizedUri = normalizeUriWithoutDatabase(uri);
        return MongoClients.create(new ConnectionString(normalizedUri));
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }

    private String normalizeUriWithoutDatabase(String rawUri) {
        String value = rawUri == null ? "" : rawUri.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("spring.data.mongodb.uri must not be empty");
        }

        int schemeIdx = value.indexOf("://");
        int searchStart = schemeIdx >= 0 ? schemeIdx + 3 : 0;
        int slashIdx = value.indexOf('/', searchStart);
        if (slashIdx < 0) {
            return value;
        }

        int queryIdx = value.indexOf('?', slashIdx);
        if (queryIdx < 0) {
            return value.substring(0, slashIdx) + "/";
        }

        return value.substring(0, slashIdx + 1) + "?" + value.substring(queryIdx + 1);
    }
}
