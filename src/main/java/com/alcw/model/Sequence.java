package com.alcw.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "database_sequences")
public class Sequence {
    @Id
    private String id;
    private int value;
    private long seq; // Add this field for blog sequences

    // Helper method to get the appropriate value based on field name
    public long getValue(String fieldName) {
        if ("seq".equals(fieldName)) {
            return seq;
        } else {
            return value;
        }
    }
}
