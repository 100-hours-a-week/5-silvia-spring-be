package com.avoworld.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.avoworld.entity.Post;
import com.avoworld.entity.Comment;
import com.avoworld.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final AmazonS3 s3Client;
    private final PostService postService;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public PostController(AmazonS3 s3Client, PostService postService, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.postService = postService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{postId}")
    public Post getPostById(@PathVariable("postId") Long postId) {
        return postService.getPostById(postId.intValue());
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable("postId") Long postId) {
        postService.deletePost(postId.intValue());
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> createPost(@RequestParam("file") MultipartFile file, @RequestParam("data") MultipartFile data) throws IOException, ServletException {
        String postJson = extractJsonFromFile(data);
        Post post = parsePostJson(postJson);
        postService.createPost(post, file);  // Updated to pass both post and file
        return ResponseEntity.ok("Post created successfully");
    }

    @PutMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ResponseEntity<String> updatePost(@PathVariable("postId") Long postId,
                                             @RequestParam(value = "file", required = false) MultipartFile file,
                                             @RequestParam(value = "postPicture", required = false) String postPicture,
                                             @RequestParam("data") String postJson) throws IOException, ServletException {
        Post post = parsePostJson(postJson);
        post.setId(postId.intValue());

        if (file != null && !file.isEmpty()) {
            postService.updatePost(post, file);  // Updated to pass both post and file
        } else {
            postService.updatePostWithoutFile(post, postPicture);
        }
        return ResponseEntity.ok("Post updated successfully");
    }

    private String extractJsonFromFile(MultipartFile data) throws IOException, ServletException {
        String postJson;
        try (InputStream inputStream = data.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            postJson = bufferedReader.lines().collect(Collectors.joining("\n"));
        }

        if (postJson == null || postJson.isEmpty()) {
            throw new ServletException("Data parameter is missing");
        }

        return postJson;
    }

    private Post parsePostJson(String postJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.readValue(postJson, Post.class);
    }

    @PutMapping("/{postId}/views")
    public void incrementPostViews(@PathVariable("postId") Long postId) {
        postService.incrementPostViews(postId);
    }

    @GetMapping("/{postId}/comments")
    public List<Comment> getCommentsByPostId(@PathVariable("postId") Long postId) {
        return postService.getCommentsByPostId(postId);
    }

    @PostMapping("/{postId}/comments")
    public void createComment(@PathVariable("postId") Long postId, @RequestBody Comment comment) {
        comment.setPostId(postId.intValue());
        postService.createComment(comment);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public void updateComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, @RequestBody Comment comment) {
        comment.setId(commentId.intValue());
        comment.setPostId(postId.intValue());
        postService.updateComment(comment);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public void deleteComment(@PathVariable("commentId") Long commentId) {
        postService.deleteComment(commentId);
    }
}
