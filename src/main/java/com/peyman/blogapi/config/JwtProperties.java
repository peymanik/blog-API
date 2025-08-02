package com.peyman.blogapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String activeKeyId;
    private List<JwtKey> keys;

    @Data
    public static class JwtKey {
        private String id;
        private String publicKey;
        private String privateKey;
    }
}
