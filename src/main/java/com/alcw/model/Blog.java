package com.alcw.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "blogs")
public class Blog {
    @Id
    private String id;
    private String blogId; // Auto-generated ALCBIDXXXX
    private String title;
    private String author;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private List<Section> sections = new ArrayList<>();

    @Data
    public static class Section {
        private String heading;
        private String subHeading;
        private String body;
        private List<String> images = new ArrayList<>();
        private List<String> references = new ArrayList<>();
    }
}
