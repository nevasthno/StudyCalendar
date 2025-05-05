package com.example.demo.javaSrc.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.javaSrc.people.PeopleRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtils jwtUtils,
                                                           UserDetailsService uds) {
        return new JwtAuthenticationFilter(jwtUtils, uds);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter) throws Exception {
        http
          .csrf(cs -> cs.disable())
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/", "/login.html", "/main.html", "/teacher.html",
                               "/styles/**", "/scripts/**", "/favicon.ico")
                .permitAll()
              .requestMatchers(HttpMethod.POST, "/api/login")
                .permitAll()
              .anyRequest()
                .authenticated()
          )
          .exceptionHandling(ex -> ex
              .authenticationEntryPoint((req, res, e) -> {
                  res.setStatus(401);
                  res.setContentType("application/json");
                  res.getWriter().write("{\"error\":\"Unauthorized\"}");
              })
          )
          .addFilterBefore(jwtFilter,
                           UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) 
            throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(PeopleRepository repo) {
        return username -> repo.findByEmail(username)
            .map(person -> User.withUsername(person.getEmail())
                               .password(person.getPassword())
                               .authorities("ROLE_" + person.getRole())
                               .build()
            )
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
