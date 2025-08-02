package com.peyman.blogapi.controller;


import com.nimbusds.jose.JOSEException;
import com.peyman.blogapi.dto.*;
import com.peyman.blogapi.security.JwtUtil;
import com.peyman.blogapi.security.RefreshTokenStore;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, RefreshTokenData> refreshTokenStore = new ConcurrentHashMap<>();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            String username = request.getUsername();

            // Generate raw XSRF token
            String xsrfToken = UUID.randomUUID().toString();

            // Hash XSRF token for JWT claim
            String xsrfTokenHashed = passwordEncoder.encode(xsrfToken);

            // Generate access token with hashed XSRF token embedded
            String accessToken = jwtUtil.generateAccessToken(username, xsrfTokenHashed);

            // Generate refresh token and store
            String refreshToken = jwtUtil.generateRefreshToken();
            String refreshTokenId = UUID.randomUUID().toString();
            Instant expiration = Instant.now().plus(Duration.ofDays(7));
            String refreshTokenKey = username + ":" + refreshTokenId;
            refreshTokenStore.put(refreshTokenKey, new RefreshTokenData(refreshToken, expiration));

            // Set refresh token cookie (HttpOnly recommended)
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(false)
                    .secure(false) // set true for production
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(Duration.between(Instant.now(), expiration))
                    .build();
            response.addHeader("Set-Cookie", refreshTokenCookie.toString());

            // Set raw XSRF token cookie (not HttpOnly, accessible by JS)
            ResponseCookie xsrfCookie = ResponseCookie.from("XSRF-TOKEN", xsrfToken)
                    .httpOnly(false)
                    .secure(false)  // set true for production
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(Duration.ofMinutes(15))
                    .build();
            response.addHeader("Set-Cookie", xsrfCookie.toString());

            return ResponseEntity.ok(new TokenResponse(accessToken));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (JOSEException e) {
            throw new RuntimeException("Token generation error", e);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            // Extract refresh token from cookie
            String refreshToken = null;
            if (httpRequest.getCookies() != null) {
                for (Cookie cookie : httpRequest.getCookies()) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing refresh token cookie");
            }

            // Find matching refresh token in store
            String matchedKey = null;
            for (Map.Entry<String, RefreshTokenData> entry : refreshTokenStore.entrySet()) {
                if (entry.getValue().getToken().equals(refreshToken)) {
                    matchedKey = entry.getKey();
                    break;
                }
            }

            if (matchedKey == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }

            String[] parts = matchedKey.split(":", 2);
            if (parts.length != 2) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token key format");
            }

            String username = parts[0];
            RefreshTokenData oldEntry = refreshTokenStore.get(matchedKey);

            if (oldEntry.getExpiration().isBefore(Instant.now())) {
                refreshTokenStore.remove(matchedKey);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired refresh token");
            }

            // Rotate refresh token
            String newRefreshToken = jwtUtil.generateRefreshToken();
            String newRefreshTokenId = UUID.randomUUID().toString();
            String newKey = username + ":" + newRefreshTokenId;

            refreshTokenStore.put(newKey, new RefreshTokenData(newRefreshToken, oldEntry.getExpiration()));
            refreshTokenStore.remove(matchedKey);

            // Set new refresh token cookie
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(false)
                    .secure(false) // set true for production
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(Duration.between(Instant.now(), oldEntry.getExpiration()))
                    .build();
            httpResponse.addHeader("Set-Cookie", refreshTokenCookie.toString());

            // Generate new raw XSRF token and its hashed version
            String xsrfToken = UUID.randomUUID().toString();
            String xsrfTokenHashed = passwordEncoder.encode(xsrfToken);

            // Generate new access token with hashed XSRF embedded
            String accessToken = jwtUtil.generateAccessToken(username, xsrfTokenHashed);

            // Set new raw XSRF token cookie
            ResponseCookie xsrfCookie = ResponseCookie.from("XSRF-TOKEN", xsrfToken)
                    .httpOnly(false)
                    .secure(false) // set true for production
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(Duration.ofMinutes(15))
                    .build();
            httpResponse.addHeader("Set-Cookie", xsrfCookie.toString());

            return ResponseEntity.ok(new TokenResponse(accessToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token refresh error");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear refresh token cookie
        ResponseCookie clearRefreshToken = ResponseCookie.from("refreshToken", "")
                .httpOnly(false)
                .secure(false) // set true for production
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", clearRefreshToken.toString());

        // Clear XSRF token cookie
        ResponseCookie clearXsrfToken = ResponseCookie.from("XSRF-TOKEN", "")
                .httpOnly(false)
                .secure(false) // set true for production
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", clearXsrfToken.toString());

        return ResponseEntity.ok("Logged out");
    }
}

