package com.portfolio.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {

        /*
         * CLOUDINARY_URL is kept outside the source code for security.
         * Locally, IntelliJ will provide this environment variable.
         * On the live website, Railway provides the same variable.
         *
         * Example format:
         * cloudinary://API_KEY:API_SECRET@CLOUD_NAME
         *
         * Never hard-code the real API secret in this Java file.
         */
        String cloudinaryUrl = System.getenv("CLOUDINARY_URL");

        /*
         * Stop the application immediately if the variable is missing.
         * This is preferable to discovering the configuration problem
         * only when an admin tries to upload a project.
         */
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            throw new IllegalStateException(
                    "CLOUDINARY_URL environment variable is not configured."
            );
        }

        /*
         * Cloudinary reads the cloud name, API key, and API secret
         * directly from the CLOUDINARY_URL value.
         */
        return new Cloudinary(cloudinaryUrl);
    }
}