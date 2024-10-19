package com.avoworld.jwt;

import com.avoworld.dto.CustomUserDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public LoginFilter(String url, AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        super(new AntPathRequestMatcher(url));
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        // 요청의 Content-Type이 application/json인지 확인
        if (!request.getContentType().equals("application/json")) {
            throw new IllegalArgumentException("Request content type is not application/json");
        }

        Map<String, String> credentials;
        try {
            credentials = new ObjectMapper().readValue(request.getInputStream(), new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new ServletException("Failed to read request payload", e);
        }

        String email = credentials.get("email");
        String password = credentials.get("password");

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);

        return authenticationManager.authenticate(token);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException {

        System.out.println("success");

        CustomUserDetails customUserDetails = (CustomUserDetails) authResult.getPrincipal();

        String email = customUserDetails.getUsername();

        String token = jwtUtil.createJwt(email, 60*60*10L);

        response.addHeader("Authorization", "Bearer " + token);

        // JSON 형식으로 응답
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"token\": \"" + token + "\"}");
    }


    @Override
    public void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse res, AuthenticationException failed)  {
        res.setStatus(401);
        System.out.println("failure");
    }
}
