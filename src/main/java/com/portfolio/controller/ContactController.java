package com.portfolio.controller;

import com.portfolio.model.ContactMessage;
import com.portfolio.repository.ContactRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactController {

    private final ContactRepository contactRepository;

    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @PostMapping("/contact")
    public String submitContact(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String message) {

        // Create contact message object
        ContactMessage contactMessage =
                new ContactMessage(name, email, message);

        // Save to MySQL database
        contactRepository.save(contactMessage);

        System.out.println("----------------------");
        System.out.println("MESSAGE SAVED TO DATABASE");
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("----------------------");

        return "redirect:/#contact";
    }
}