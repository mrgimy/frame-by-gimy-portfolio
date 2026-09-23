package com.portfolio.controller;

import com.portfolio.model.Project;
import com.portfolio.repository.ContactRepository;
import com.portfolio.repository.ProjectRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
public class AdminController {

    private final ContactRepository contactRepository;
    private final ProjectRepository projectRepository;

    public AdminController(ContactRepository contactRepository,
                           ProjectRepository projectRepository) {

        this.contactRepository = contactRepository;
        this.projectRepository = projectRepository;
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


        Path imageDirectory =
                Paths.get("uploads/images");

        Path videoDirectory =
                Paths.get("uploads/videos");

        Files.createDirectories(imageDirectory);
        Files.createDirectories(videoDirectory);


        // ================= SAVE IMAGE =================

        if (!imageFile.isEmpty()) {

            String imageName =
                    UUID.randomUUID()
                            + "_"
                            + imageFile.getOriginalFilename();

            Path imagePath =
                    imageDirectory.resolve(imageName);

            Files.copy(
                    imageFile.getInputStream(),
                    imagePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            project.setImageUrl(
                    "/uploads/images/" + imageName
            );
        }


        // ================= SAVE VIDEO =================

        if (!videoFile.isEmpty()) {

            String videoName =
                    UUID.randomUUID()
                            + "_"
                            + videoFile.getOriginalFilename();

            Path videoPath =
                    videoDirectory.resolve(videoName);

            Files.copy(
                    videoFile.getInputStream(),
                    videoPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            project.setVideoUrl(
                    "/uploads/videos/" + videoName
            );
        }


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
            @PathVariable Long id) {

        Project project =
                projectRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid project ID: " + id
                                )
                        );


        // Delete image
        deleteUploadedFile(
                project.getImageUrl()
        );


        // Delete video
        deleteUploadedFile(
                project.getVideoUrl()
        );


        // Delete database record
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


        // Get existing project first
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

            Path imageDirectory =
                    Paths.get("uploads/images");

            Files.createDirectories(
                    imageDirectory
            );


            // Save new image first
            String imageName =
                    UUID.randomUUID()
                            + "_"
                            + imageFile.getOriginalFilename();

            Path imagePath =
                    imageDirectory.resolve(
                            imageName
                    );

            Files.copy(
                    imageFile.getInputStream(),
                    imagePath,
                    StandardCopyOption.REPLACE_EXISTING
            );


            // Delete old image
            deleteUploadedFile(
                    existingProject.getImageUrl()
            );


            // Set new URL
            existingProject.setImageUrl(
                    "/uploads/images/" + imageName
            );
        }


        // =================================================
        // REPLACE VIDEO
        // =================================================

        if (!videoFile.isEmpty()) {

            Path videoDirectory =
                    Paths.get("uploads/videos");

            Files.createDirectories(
                    videoDirectory
            );


            // Save new video first
            String videoName =
                    UUID.randomUUID()
                            + "_"
                            + videoFile.getOriginalFilename();

            Path videoPath =
                    videoDirectory.resolve(
                            videoName
                    );

            Files.copy(
                    videoFile.getInputStream(),
                    videoPath,
                    StandardCopyOption.REPLACE_EXISTING
            );


            // Delete old video
            deleteUploadedFile(
                    existingProject.getVideoUrl()
            );


            // Set new URL
            existingProject.setVideoUrl(
                    "/uploads/videos/" + videoName
            );
        }


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

        submittedProject.setVideoUrl(
                existingProject.getVideoUrl()
        );
    }


    // =====================================================
    // DELETE UPLOADED FILE
    // =====================================================

    private void deleteUploadedFile(
            String fileUrl) {

        if (fileUrl == null ||
                fileUrl.isBlank()) {

            return;
        }


        // Only delete files inside /uploads/
        if (!fileUrl.startsWith("/uploads/")) {

            return;
        }


        try {

            String relativePath =
                    fileUrl.substring(
                            "/uploads/".length()
                    );

            Path uploadRoot =
                    Paths.get("uploads")
                            .toAbsolutePath()
                            .normalize();

            Path filePath =
                    uploadRoot
                            .resolve(relativePath)
                            .normalize();


            // Prevent path traversal
            if (!filePath.startsWith(uploadRoot)) {

                return;
            }


            Files.deleteIfExists(
                    filePath
            );

        } catch (IOException e) {

            System.err.println(
                    "Could not delete uploaded file: "
                            + fileUrl
            );
        }
    }


    // =====================================================
    // IMAGE VALIDATION
    // =====================================================

    private boolean isValidImage(
            MultipartFile file) {

        if (file == null ||
                file.isEmpty()) {

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

        if (file == null ||
                file.isEmpty()) {

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