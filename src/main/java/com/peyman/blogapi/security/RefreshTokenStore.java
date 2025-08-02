package com.peyman.blogapi.security;


import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RefreshTokenStore {
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    public void storeToken(String token, String username) {
        tokenStore.put(token, username);
    }

    public boolean isTokenValid(String token, String username) {
        return username.equals(tokenStore.get(token));
    }

    public void revokeToken(String token) {
        tokenStore.remove(token);
    }
}