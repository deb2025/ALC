//package com.alcw.controller;
//
//
//import com.alcw.dto.BlogDTO;
//import com.alcw.model.Blog;
//import com.alcw.service.BlogService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/blogs")
//public class BlogController {
//    private final BlogService blogService;
//
//    public BlogController(BlogService blogService) {
//        this.blogService = blogService;
//    }
//
//    @PostMapping
//    public ResponseEntity<Blog> createBlog(@ModelAttribute BlogDTO blogDTO) {
//        Blog blog = blogService.createBlog(blogDTO);
//        return ResponseEntity.ok(blog);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<Blog> updateBlog(
//            @PathVariable String id,
//            @ModelAttribute BlogDTO blogDTO) {
//        Blog blog = blogService.updateBlog(id, blogDTO);
//        return ResponseEntity.ok(blog);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteBlog(@PathVariable String id) {
//        blogService.deleteBlog(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Blog> getBlogById(@PathVariable String id) {
//        Blog blog = blogService.getBlogById(id);
//        return ResponseEntity.ok(blog);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Blog>> getAllBlogs() {
//        List<Blog> blogs = blogService.getAllBlogs();
//        return ResponseEntity.ok(blogs);
//    }
//}