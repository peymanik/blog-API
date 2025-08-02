package com.peyman.blogapi.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.text.ParseException;
import java.util.Collections;

//@Component
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter implements Filter {
//
//    private final JwtUtil jwtUtil;
//
//    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);  // Inject this via constructor or create bean
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException, java.io.IOException {
//
//        HttpServletRequest httpReq = (HttpServletRequest) request;
//        HttpServletResponse httpRes = (HttpServletResponse) response;
//
//        try {
//            String authHeader = httpReq.getHeader("Authorization");
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                // No token, just continue filter chain
//                chain.doFilter(request, response);
//                return;
//            }
//
//            String token = authHeader.substring(7);
//            SignedJWT signedJWT = jwtUtil.validateToken(token);
//
//            // 1️⃣ Extract hashed XSRF token from JWT claim
//            String hashedXsrfFromJwt = (String) signedJWT.getJWTClaimsSet().getClaim("xsrf");
//            if (hashedXsrfFromJwt == null) {
//                httpRes.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing XSRF claim in JWT");
//                return;
//            }
//
//            // 2️⃣ Extract raw XSRF token from cookie
//            String rawXsrfFromCookie = null;
//            if (httpReq.getCookies() != null) {
//                for (Cookie cookie : httpReq.getCookies()) {
//                    if ("XSRF-TOKEN".equals(cookie.getName())) {
//                        rawXsrfFromCookie = cookie.getValue();
//                        break;
//                    }
//                }
//            }
//
//            if (rawXsrfFromCookie == null) {
//                httpRes.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing XSRF-TOKEN cookie");
//                return;
//            }
//
//            // 3️⃣ Validate raw cookie token matches hashed JWT token
//            if (!passwordEncoder.matches(rawXsrfFromCookie, hashedXsrfFromJwt)) {
//                httpRes.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid XSRF token");
//                return;
//            }
//
//            // 4️⃣ Set authentication in security context
//            String username = signedJWT.getJWTClaimsSet().getSubject();
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        } catch (Exception e) {
//            httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
//            return;
//        }
//
//        // Continue filter chain
//        chain.doFilter(request, response);
//    }
//}

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return (path.equals("/auth/login") && method.equals("POST"))
                || (path.equals("/auth/refresh") && method.equals("POST"))
                || (path.equals("/public/users") && method.equals("POST"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException, java.io.IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            SignedJWT signedJWT = jwtUtil.validateToken(token);

            String hashedXsrfFromJwt = (String) signedJWT.getJWTClaimsSet().getClaim("xsrf");
            if (hashedXsrfFromJwt == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing XSRF claim in JWT");
                return;
            }

            String rawXsrfFromCookie = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("XSRF-TOKEN".equals(cookie.getName())) {
                        rawXsrfFromCookie = cookie.getValue();
                        break;
                    }
                }
            }

            if (rawXsrfFromCookie == null || !passwordEncoder.matches(rawXsrfFromCookie, hashedXsrfFromJwt)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing XSRF token");
                return;
            }

            String username = signedJWT.getJWTClaimsSet().getSubject();
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        chain.doFilter(request, response);
    }
}
