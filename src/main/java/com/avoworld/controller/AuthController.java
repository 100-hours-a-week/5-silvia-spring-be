package com.avoworld.controller;

import com.avoworld.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.avoworld.service.AuthService;
import com.avoworld.entity.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JWTUtil jwtUtil;

    private static final String BEARER_PREFIX = "Bearer ";

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User loginUser) {
        try {
            String token = authService.loginUser(loginUser);
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> getEmailFromToken(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.getEmail(token.replace("Bearer ", ""));
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Collections.singletonMap("error", "Invalid token"));
        }
    }

    // 로그아웃 다시
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith(BEARER_PREFIX)) {
                return ResponseEntity.status(400).body(Collections.singletonMap("error", "Invalid Authorization header"));
            }

            String cleanedToken = token.substring(BEARER_PREFIX.length());
            authService.invalidateToken(cleanedToken);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully logged out");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the exception with a proper logging framework
            e.printStackTrace(); // Replace with proper logging
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Logout failed due to server error"));
        }
    }

}




//    @PostMapping("/login")
//    public ResponseEntity<Map<String, Object>> login(@RequestBody User loginUser) {
//        try {
//            String token = authService.loginUser(loginUser);
//            String email = jwtUtil.getEmail(token);
//            Map<String, Object> response = new HashMap<>();
//            response.put("token", token);
//            response.put("email", email); // 로그인한 사용자의 이메일을 포함
//            log.info("User email from token: {}", email);
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(401).body(Collections.singletonMap("error", e.getMessage()));
//        }
//    }



//    @PostMapping("/login")
//    public ResponseEntity<String> login(@RequestBody User loginUser) {
//        try {
//            String token = authService.loginUser(loginUser);
//            return ResponseEntity.ok(token);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(401).body(e.getMessage());
//        }
//    }



