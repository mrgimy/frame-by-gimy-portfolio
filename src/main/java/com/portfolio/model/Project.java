package com.portfolio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String category;

    // Cloudinary secure image URL
    @Column(length = 1000)
    private String imageUrl;

    // Cloudinary image public ID
    private String imagePublicId;

    // Cloudinary secure video URL
    @Column(length = 1000)
    private String videoUrl;

    // Cloudinary video public ID
    private String videoPublicId;

    // Featured project or normal project
    private boolean featured;

    // Controls the order projects appear on the website
    private Integer displayOrder;


    // ================= CONSTRUCTOR =================

    public Project() {
    }


    public Project(String title,
                   String description,
                   String category,
                   String imageUrl,
                   String videoUrl) {

        this.title = title;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
    }


    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public String getImagePublicId() {
        return imagePublicId;
    }

    public void setImagePublicId(String imagePublicId) {
        this.imagePublicId = imagePublicId;
    }


    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }


    public String getVideoPublicId() {
        return videoPublicId;
    }

    public void setVideoPublicId(String videoPublicId) {
        this.videoPublicId = videoPublicId;
    }


    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }


    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}