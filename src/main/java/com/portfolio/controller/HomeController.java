package com.portfolio.controller;

import com.portfolio.repository.ProjectRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProjectRepository projectRepository;

    public HomeController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute(
                "projects",
                projectRepository.findAllByOrderByDisplayOrderAsc()
        );

        return "index";
    }
}