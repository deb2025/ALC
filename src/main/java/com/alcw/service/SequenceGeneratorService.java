package com.alcw.service;

import com.alcw.model.Sequence;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class SequenceGeneratorService {
    private final MongoOperations mongoOperations;

    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    // Existing method for sequences using "value" field
    public int generateSequence(String seqName) {
        Sequence counter = mongoOperations.findAndModify(
                Query.query(where("_id").is(seqName)),
                new Update().inc("value", 1),
                options().returnNew(true).upsert(true),
                Sequence.class
        );
        return counter.getValue();
    }

    // New method for blog sequence using "seq" field
    public int generateBlogSequence(String seqName) {
        DatabaseSequence counter = mongoOperations.findAndModify(
                Query.query(where("_id").is(seqName)),
                new Update().inc("seq", 1),
                options().returnNew(true).upsert(true),
                DatabaseSequence.class
        );
        return counter != null ? (int) counter.getSeq() : 1;
    }

    // Overloaded method with default field name
    public int generateSequence(String seqName, String fieldName) {
        if ("seq".equals(fieldName)) {
            return generateBlogSequence(seqName);
        } else {
            return generateSequence(seqName); // default to "value" field
        }
    }
}

// Add this new class for sequences using "seq" field
@Data
@Document(collection = "database_sequences")
class DatabaseSequence {
    @Id
    private String id;
    private long seq;
}