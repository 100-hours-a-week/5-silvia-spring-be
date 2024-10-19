package com.avoworld.config;

import com.avoworld.jwt.JWTFilter;
import com.avoworld.jwt.JWTUtil;
import com.avoworld.jwt.JoinFilter;
import com.avoworld.jwt.LoginFilter;
import com.avoworld.service.AuthService;
import com.avoworld.service.CustomUserDetailsService;
import com.avoworld.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JWTUtil jwtUtil;
    private final AuthService authService;
    private final FileStorageService fileStorageService;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService, JWTUtil jwtUtil, @Lazy AuthService authService, @Lazy FileStorageService fileStorageService) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.fileStorageService = fileStorageService;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = authenticationManager(http.getSharedObject(AuthenticationConfiguration.class));

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/", "/api/join", "/api/accounts/check-email", "/api/posts/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new LoginFilter("/api/login", authenticationManager, jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JoinFilter("/api/join", authenticationManager, authService, jwtUtil, fileStorageService), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://avoworld-bucket.s3-website.ap-northeast-2.amazonaws.com",
                "http://localhost:3000",
                "https://d1jlocd3s0jxr6.cloudfront.net",
                "http://d1jlocd3s0jxr6.cloudfront.net"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
