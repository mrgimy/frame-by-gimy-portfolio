package com.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;


    // ================= PASSWORD ENCODER =================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // ================= SECURITY =================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/contact",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/videos/**",
                                "/uploads/**"
                        ).permitAll()

                        .requestMatchers("/admin/**").authenticated()

                        .anyRequest().permitAll()
                )

                .formLogin(form -> form

                        .loginPage("/admin/login")

                        .loginProcessingUrl("/admin/login")

                        .defaultSuccessUrl("/admin", true)

                        .permitAll()
                )

                .logout(logout -> logout

                        .logoutUrl("/admin/logout")

                        .logoutSuccessUrl("/")

                        .permitAll()
                );

        return http.build();
    }


    // ================= ADMIN USER =================

    @Bean
    public InMemoryUserDetailsManager userDetailsService(
            PasswordEncoder passwordEncoder) {

        UserDetails admin = User.withUsername(adminUsername)

                .password(
                        passwordEncoder.encode(adminPassword)
                )

                .roles("ADMIN")

                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}