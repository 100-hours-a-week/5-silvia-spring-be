package com.avoworld.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "community_post")
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private int id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "article", columnDefinition="LONGTEXT")
    private String article;

    @Column(name = "post_picture")
    private String postPicture;

    @Column(name = "edited_post_title", nullable = true)
    private String editedPostTitle;

    @Column(name = "edited_post_content")
    private String editedPostContent;

    @Column(name = "views")
    private int views;

    @Column(name = "likes")
    private int likes;

    private String fileUrl;

    @Column(name = "create_at", nullable = false, updatable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createAt;


    @Column(name = "update_at", nullable = true)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updateAt;



    @PrePersist
    protected void onCreate() {
        if (this.createAt == null) {
            this.createAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
