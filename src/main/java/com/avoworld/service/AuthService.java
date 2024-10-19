package com.avoworld.service;

import com.avoworld.entity.User;
import com.avoworld.jwt.JWTUtil;
import com.avoworld.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private Set<String> tokenBlacklist = new HashSet<>();


    @Autowired
    public AuthService(UserRepository userRepository, JWTUtil jwtUtil, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public String loginUser(User loginUser) {
        User user = userRepository.findByEmail(loginUser.getEmail());
        if (user != null) {
            if (bCryptPasswordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
                return jwtUtil.createJwt(user.getEmail(), 60 * 60 * 10L);
            } else {
                throw new RuntimeException("Invalid password");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("User already exists");
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void invalidateToken(String token) {
        tokenBlacklist.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    public boolean isTokenValid(String token) {
        return !isTokenBlacklisted(token);
    }
}
