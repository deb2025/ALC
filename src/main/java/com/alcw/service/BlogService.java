//package com.alcw.service;
//
//import com.alcw.dto.BlogDTO;
//import com.alcw.model.Blog;
//import com.alcw.repository.BlogRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class BlogService {
//    private final BlogRepository blogRepository;
//    private final CloudinaryService cloudinaryService;
//    private final SequenceGeneratorService sequenceGeneratorService;
//
//    public Blog createBlog(BlogDTO blogDTO) {
//        Blog blog = new Blog();
//        // Generate blog ID
//        String blogId = "ALCBID" + String.format("%04d", sequenceGeneratorService.generateSequence(Blog.SEQUENCE_NAME));
//        blog.setBlogId(blogId);
//        mapDtoToBlog(blogDTO, blog);
//        return blogRepository.save(blog);
//    }
//
//
//    public Blog updateBlog(String id, BlogDTO blogDTO) {
//        Blog blog = blogRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Blog not found"));
//        updateBlogFromDto(blogDTO, blog); // Use the new update method
//        blog.setUpdatedAt(LocalDateTime.now());
//        return blogRepository.save(blog);
//    }
//
//    public Blog updateBlogByBlogId(String blogId, BlogDTO blogDTO) {
//        Blog blog = blogRepository.findByBlogId(blogId)
//                .orElseThrow(() -> new RuntimeException("Blog not found with blogId: " + blogId));
//        updateBlogFromDto(blogDTO, blog);
//        blog.setUpdatedAt(LocalDateTime.now());
//        return blogRepository.save(blog);
//    }
//
//    public void deleteBlog(String id) {
//        blogRepository.deleteById(id);
//    }
//
//    public void deleteBlogByBlogId(String blogId) {
//        Blog blog = blogRepository.findByBlogId(blogId)
//                .orElseThrow(() -> new RuntimeException("Blog not found with blogId: " + blogId));
//        blogRepository.delete(blog);
//    }
//
//    public Blog getBlogById(String id) {
//        return blogRepository.findById(id).orElse(null);
//    }
//
//    public List<Blog> getAllBlogs() {
//        return blogRepository.findAll();
//    }
//
//    public Blog getBlogByBlogId(String blogId) {
//        return blogRepository.findByBlogId(blogId).orElse(null);
//    }
//
//    private void mapDtoToBlog(BlogDTO dto, Blog blog) {
//        blog.setTitle(dto.getTitle());
//        blog.setAuthor(dto.getAuthor());
//
//        // Process sections
//        for (BlogDTO.SectionDTO sectionDto : dto.getSections()) {
//            Blog.Section section = new Blog.Section();
//            section.setHeading(sectionDto.getHeading());
//            section.setSubHeading(sectionDto.getSubHeading());
//            section.setBody(sectionDto.getBody());
//            section.setReferences(sectionDto.getReferences());
//
//            // Upload images to Cloudinary
//            if (sectionDto.getImages() != null && !sectionDto.getImages().isEmpty()) {
//                List<String> imageUrls = sectionDto.getImages().stream()
//                        .map(cloudinaryService::uploadFile)
//                        .collect(Collectors.toList());
//                section.setImages(imageUrls);
//            }
//
//            blog.getSections().add(section);
//        }
//    }
//
//    private void updateBlogFromDto(BlogDTO dto, Blog blog) {
//        // Update title if provided
//        if (dto.getTitle() != null) {
//            blog.setTitle(dto.getTitle());
//        }
//
//        // Update author if provided
//        if (dto.getAuthor() != null) {
//            blog.setAuthor(dto.getAuthor());
//        }
//
//        // Update sections if provided
//        if (dto.getSections() != null && !dto.getSections().isEmpty()) {
//            // For simplicity, we're replacing all sections
//            // For a more sophisticated approach, you could match sections by ID
//            blog.getSections().clear();
//
//            for (BlogDTO.SectionDTO sectionDto : dto.getSections()) {
//                Blog.Section section = new Blog.Section();
//                section.setHeading(sectionDto.getHeading());
//                section.setSubHeading(sectionDto.getSubHeading());
//                section.setBody(sectionDto.getBody());
//                section.setReferences(sectionDto.getReferences());
//
//                // Upload images to Cloudinary if provided
//                if (sectionDto.getImages() != null && !sectionDto.getImages().isEmpty()) {
//                    List<String> imageUrls = sectionDto.getImages().stream()
//                            .map(cloudinaryService::uploadFile)
//                            .collect(Collectors.toList());
//                    section.setImages(imageUrls);
//                }
//
//                blog.getSections().add(section);
//            }
//        }
//    }
//}