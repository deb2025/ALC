package com.alcw.repository;

import com.alcw.model.Blog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface BlogRepository extends MongoRepository<Blog, String> {
    Optional<Blog> findByBlogId(String blogId);
}