package com.alcw.service;

import com.alcw.dto.BlogDTO;
import com.alcw.model.Blog;
import com.alcw.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {
    private final BlogRepository blogRepository;
    private final CloudinaryService cloudinaryService;
    private final SequenceGeneratorService sequenceGeneratorService;

    public Blog createBlog(BlogDTO blogDTO) {
        Blog blog = new Blog();
        // Generate blog ID
        String blogId = "ALCBID" + String.format("%04d", sequenceGeneratorService.generateSequence(Blog.SEQUENCE_NAME));
        blog.setBlogId(blogId);
        mapDtoToBlog(blogDTO, blog);
        return blogRepository.save(blog);
    }

    public Blog updateBlog(String id, BlogDTO blogDTO) {
        Blog blog = blogRepository.findById(id)
                .or(() -> blogRepository.findByBlogId(id))
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));
        updateBlogFromDto(blogDTO, blog);
        blog.setUpdatedAt(LocalDateTime.now());
        return blogRepository.save(blog);
    }

    public Blog updateBlogByBlogId(String blogId, BlogDTO blogDTO) {
        Blog blog = blogRepository.findByBlogId(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with blogId: " + blogId));
        updateBlogFromDto(blogDTO, blog);
        blog.setUpdatedAt(LocalDateTime.now());
        return blogRepository.save(blog);
    }

    public void deleteBlog(String id) {
        Optional<Blog> blog = blogRepository.findById(id).or(() -> blogRepository.findByBlogId(id));
        if (blog.isPresent()) {
            blogRepository.delete(blog.get());
        } else {
            throw new RuntimeException("Blog not found with id: " + id);
        }
    }

    public void deleteBlogByBlogId(String blogId) {
        Blog blog = blogRepository.findByBlogId(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with blogId: " + blogId));
        blogRepository.delete(blog);
    }

    public Blog getBlogById(String id) {
        return blogRepository.findById(id)
                .or(() -> blogRepository.findByBlogId(id))
                .orElse(null);
    }

    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    public Blog getBlogByBlogId(String blogId) {
        return blogRepository.findByBlogId(blogId).orElse(null);
    }

    private void mapDtoToBlog(BlogDTO dto, Blog blog) {
        blog.setTitle(dto.getTitle());
        blog.setAuthor(dto.getAuthor());

        if (dto.getSections() != null) {
            for (BlogDTO.SectionDTO sectionDto : dto.getSections()) {
                Blog.Section section = new Blog.Section();
                section.setHeading(sectionDto.getHeading());
                section.setSubHeading(sectionDto.getSubHeading());
                section.setBody(sectionDto.getBody());
                section.setReferences(sectionDto.getReferences() != null ? sectionDto.getReferences() : new ArrayList<>());

                // Upload images to Cloudinary if provided
                if (sectionDto.getImages() != null && !sectionDto.getImages().isEmpty()) {
                    List<String> imageUrls = sectionDto.getImages().stream()
                            .filter(img -> img != null && !img.isEmpty())
                            .map(cloudinaryService::uploadFile)
                            .collect(Collectors.toList());
                    section.setImages(imageUrls);
                }

                blog.getSections().add(section);
            }
        }
    }

    private void updateBlogFromDto(BlogDTO dto, Blog blog) {
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            blog.setTitle(dto.getTitle());
        }

        if (dto.getAuthor() != null && !dto.getAuthor().isBlank()) {
            blog.setAuthor(dto.getAuthor());
        }

        if (dto.getSections() != null && !dto.getSections().isEmpty()) {
            blog.getSections().clear();

            for (BlogDTO.SectionDTO sectionDto : dto.getSections()) {
                Blog.Section section = new Blog.Section();
                section.setHeading(sectionDto.getHeading());
                section.setSubHeading(sectionDto.getSubHeading());
                section.setBody(sectionDto.getBody());
                section.setReferences(sectionDto.getReferences() != null ? sectionDto.getReferences() : new ArrayList<>());

                if (sectionDto.getImages() != null && !sectionDto.getImages().isEmpty()) {
                    List<String> imageUrls = sectionDto.getImages().stream()
                            .filter(img -> img != null && !img.isEmpty())
                            .map(cloudinaryService::uploadFile)
                            .collect(Collectors.toList());
                    section.setImages(imageUrls);
                }

                blog.getSections().add(section);
            }
        }
    }
}