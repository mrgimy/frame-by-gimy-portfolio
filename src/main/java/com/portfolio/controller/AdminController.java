package com.portfolio.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.portfolio.model.Project;
import com.portfolio.repository.ContactRepository;
import com.portfolio.repository.ProjectRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
public class AdminController {

    private final ContactRepository contactRepository;
    private final ProjectRepository projectRepository;
    private final Cloudinary cloudinary;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AdminController(
            ContactRepository contactRepository,
            ProjectRepository projectRepository,
            Cloudinary cloudinary) {

        this.contactRepository = contactRepository;
        this.projectRepository = projectRepository;
        this.cloudinary = cloudinary;
    }


    // =====================================================
    // ADMIN LOGIN
    // =====================================================

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "login";
    }


    // =====================================================
    // ADMIN DASHBOARD
    // =====================================================

    @GetMapping("/admin")
    public String adminDashboard(Model model) {

        model.addAttribute(
                "messages",
                contactRepository.findAll()
        );

        model.addAttribute(
                "messageCount",
                contactRepository.count()
        );

        model.addAttribute(
                "projects",
                projectRepository.findAll()
        );

        model.addAttribute(
                "projectCount",
                projectRepository.count()
        );

        return "admin";
    }


    // =====================================================
    // SHOW ADD PROJECT FORM
    // =====================================================

    @GetMapping("/admin/projects/add")
    public String showAddProjectForm(Model model) {

        model.addAttribute(
                "project",
                new Project()
        );

        return "add-project";
    }


    // =====================================================
    // ADD PROJECT
    // =====================================================

    @PostMapping("/admin/projects/add")
    public String saveProject(
            @ModelAttribute Project project,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("videoFile") MultipartFile videoFile,
            Model model
    ) throws IOException {


        // ================= VALIDATE IMAGE =================

        if (!isValidImage(imageFile)) {

            model.addAttribute(
                    "errorMessage",
                    "Invalid image file. Only JPG, JPEG, PNG and WEBP are allowed."
            );

            return "add-project";
        }


        // ================= VALIDATE VIDEO =================

        if (!isValidVideo(videoFile)) {

            model.addAttribute(
                    "errorMessage",
                    "Invalid video file. Only MP4, MOV and WEBM are allowed."
            );

            return "add-project";
        }


        // ================= UPLOAD IMAGE =================

        if (!imageFile.isEmpty()) {

            Map<?, ?> imageResult =
                    cloudinary.uploader().upload(
                            imageFile.getBytes(),
                            ObjectUtils.asMap(
                                    "folder", "frame-by-gimy/images",
                                    "resource_type", "image"
                            )
                    );

            project.setImageUrl(
                    imageResult.get("secure_url").toString()
            );

            project.setImagePublicId(
                    imageResult.get("public_id").toString()
            );
        }


        // ================= UPLOAD VIDEO =================

        if (!videoFile.isEmpty()) {

            Map<?, ?> videoResult =
                    cloudinary.uploader().upload(
                            videoFile.getBytes(),
                            ObjectUtils.asMap(
                                    "folder", "frame-by-gimy/videos",
                                    "resource_type", "video"
                            )
                    );

            project.setVideoUrl(
                    videoResult.get("secure_url").toString()
            );

            project.setVideoPublicId(
                    videoResult.get("public_id").toString()
            );
        }


        // ================= SAVE DATABASE =================

        projectRepository.save(project);

        return "redirect:/admin";
    }


    // =====================================================
    // DELETE CONTACT MESSAGE
    // =====================================================

    @PostMapping("/admin/delete/{id}")
    public String deleteMessage(
            @PathVariable Long id) {

        contactRepository.deleteById(id);

        return "redirect:/admin";
    }


    // =====================================================
    // DELETE PROJECT
    // =====================================================

    @PostMapping("/admin/projects/delete/{id}")
    public String deleteProject(
            @PathVariable Long id) throws IOException {

        Project project =
                projectRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid project ID: " + id
                                )
                        );


        // ================= DELETE CLOUDINARY IMAGE =================

        deleteCloudinaryFile(
                project.getImagePublicId(),
                "image"
        );


        // ================= DELETE CLOUDINARY VIDEO =================

        deleteCloudinaryFile(
                project.getVideoPublicId(),
                "video"
        );


        // ================= DELETE DATABASE RECORD =================

        projectRepository.delete(project);

        return "redirect:/admin";
    }


    // =====================================================
    // SHOW EDIT PROJECT FORM
    // =====================================================

    @GetMapping("/admin/projects/edit/{id}")
    public String showEditProjectForm(
            @PathVariable Long id,
            Model model) {

        Project project =
                projectRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid project ID: " + id
                                )
                        );

        model.addAttribute(
                "project",
                project
        );

        return "edit-project";
    }


    // =====================================================
    // UPDATE PROJECT
    // =====================================================

    @PostMapping("/admin/projects/edit/{id}")
    public String updateProject(
            @PathVariable Long id,
            @ModelAttribute Project submittedProject,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("videoFile") MultipartFile videoFile,
            Model model
    ) throws IOException {


        Project existingProject =
                projectRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid project ID: " + id
                                )
                        );


        // ================= VALIDATE IMAGE =================

        if (!isValidImage(imageFile)) {

            copyExistingMedia(
                    submittedProject,
                    existingProject
            );

            model.addAttribute(
                    "project",
                    submittedProject
            );

            model.addAttribute(
                    "errorMessage",
                    "Invalid image file. Only JPG, JPEG, PNG and WEBP are allowed."
            );

            return "edit-project";
        }


        // ================= VALIDATE VIDEO =================

        if (!isValidVideo(videoFile)) {

            copyExistingMedia(
                    submittedProject,
                    existingProject
            );

            model.addAttribute(
                    "project",
                    submittedProject
            );

            model.addAttribute(
                    "errorMessage",
                    "Invalid video file. Only MP4, MOV and WEBM are allowed."
            );

            return "edit-project";
        }


        // ================= UPDATE DETAILS =================

        existingProject.setTitle(
                submittedProject.getTitle()
        );

        existingProject.setDescription(
                submittedProject.getDescription()
        );

        existingProject.setCategory(
                submittedProject.getCategory()
        );

        existingProject.setFeatured(
                submittedProject.isFeatured()
        );

        existingProject.setDisplayOrder(
                submittedProject.getDisplayOrder()
        );


        // =================================================
        // REPLACE IMAGE
        // =================================================

        if (!imageFile.isEmpty()) {

            Map<?, ?> imageResult =
                    cloudinary.uploader().upload(
                            imageFile.getBytes(),
                            ObjectUtils.asMap(
                                    "folder", "frame-by-gimy/images",
                                    "resource_type", "image"
                            )
                    );


            // Upload succeeded, so old image can now be removed
            deleteCloudinaryFile(
                    existingProject.getImagePublicId(),
                    "image"
            );


            existingProject.setImageUrl(
                    imageResult.get("secure_url").toString()
            );

            existingProject.setImagePublicId(
                    imageResult.get("public_id").toString()
            );
        }


        // =================================================
        // REPLACE VIDEO
        // =================================================

        if (!videoFile.isEmpty()) {

            Map<?, ?> videoResult =
                    cloudinary.uploader().upload(
                            videoFile.getBytes(),
                            ObjectUtils.asMap(
                                    "folder", "frame-by-gimy/videos",
                                    "resource_type", "video"
                            )
                    );


            // Upload succeeded, so old video can now be removed
            deleteCloudinaryFile(
                    existingProject.getVideoPublicId(),
                    "video"
            );


            existingProject.setVideoUrl(
                    videoResult.get("secure_url").toString()
            );

            existingProject.setVideoPublicId(
                    videoResult.get("public_id").toString()
            );
        }


        // ================= SAVE DATABASE =================

        projectRepository.save(
                existingProject
        );

        return "redirect:/admin";
    }


    // =====================================================
    // COPY EXISTING MEDIA FOR EDIT ERROR PAGE
    // =====================================================

    private void copyExistingMedia(
            Project submittedProject,
            Project existingProject) {

        submittedProject.setId(
                existingProject.getId()
        );

        submittedProject.setImageUrl(
                existingProject.getImageUrl()
        );

        submittedProject.setImagePublicId(
                existingProject.getImagePublicId()
        );

        submittedProject.setVideoUrl(
                existingProject.getVideoUrl()
        );

        submittedProject.setVideoPublicId(
                existingProject.getVideoPublicId()
        );
    }


    // =====================================================
    // DELETE CLOUDINARY FILE
    // =====================================================

    private void deleteCloudinaryFile(
            String publicId,
            String resourceType) throws IOException {

        if (publicId == null || publicId.isBlank()) {
            return;
        }

        cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap(
                        "resource_type", resourceType,
                        "invalidate", true
                )
        );
    }


    // =====================================================
    // IMAGE VALIDATION
    // =====================================================

    private boolean isValidImage(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return true;
        }

        String contentType =
                file.getContentType();

        if (contentType == null) {
            return false;
        }

        return contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp");
    }


    // =====================================================
    // VIDEO VALIDATION
    // =====================================================

    private boolean isValidVideo(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return true;
        }

        String contentType =
                file.getContentType();

        if (contentType == null) {
            return false;
        }

        return contentType.equals("video/mp4")
                || contentType.equals("video/quicktime")
                || contentType.equals("video/webm");
    }
}