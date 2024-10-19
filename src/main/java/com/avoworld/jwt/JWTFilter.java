package com.avoworld.jwt;

// JWT 검증 필터
// filter chain에 요청에 담긴 JWT를 검증하기 위한 커스텀 필터 등록하기
// 세션은 stateless로 관리되기 때문에 해당 요청이 끝나면 소멸됨
import com.avoworld.dto.CustomUserDetails;
import com.avoworld.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();

        AntPathMatcher pathMatcher = new AntPathMatcher();

        return pathMatcher.match("/api/join", requestURI) ||
               pathMatcher.match("/api/accounts/**", requestURI) ||
               pathMatcher.match("/uploads/**", requestURI) ||
               pathMatcher.match("/api/posts/**", requestURI); 
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            System.out.println("token expired");
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.getEmail(token);

        User user = new User();
        user.setEmail(email);
        user.setPassword("temppassword");

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        Authentication authToken  = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
