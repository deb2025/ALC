package com.alcw.controller;

import com.alcw.dto.ApiResponse;
import com.alcw.dto.BlogDTO;
import com.alcw.model.Blog;
import com.alcw.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Blog> createBlog(@ModelAttribute BlogDTO blogDTO) {
        Blog blog = blogService.createBlog(blogDTO);
        return ResponseEntity.ok(blog);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @PostMapping("/json")
    public ResponseEntity<Blog> createBlogJson(@RequestBody BlogDTO blogDTO) {
        Blog blog = blogService.createBlog(blogDTO);
        return ResponseEntity.ok(blog);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Blog> updateBlog(
            @PathVariable String id,
            @ModelAttribute BlogDTO blogDTO) {
        Blog blog = blogService.updateBlog(id, blogDTO);
        return ResponseEntity.ok(blog);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @PutMapping("/{id}/json")
    public ResponseEntity<Blog> updateBlogJson(
            @PathVariable String id,
            @RequestBody BlogDTO blogDTO) {
        Blog blog = blogService.updateBlog(id, blogDTO);
        return ResponseEntity.ok(blog);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBlog(@PathVariable String id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok(Map.of("message", "Blog deleted successfully", "id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable String id) {
        Blog blog = blogService.getBlogById(id);
        if (blog == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(blog);
    }

    @GetMapping
    public ResponseEntity<List<Blog>> getAllBlogs() {
        List<Blog> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }
}