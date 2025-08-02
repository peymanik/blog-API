package com.peyman.blogapi.security;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.peyman.blogapi.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
//import lombok.Value;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

//import javax.annotation.PostConstruct;
import java.security.*;
import java.security.spec.*;
import java.text.ParseException;
import java.util.*;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final Map<String, OctetKeyPair> jwkKeys = new HashMap<>();

    @PostConstruct
    public void initKeys() {
        for (JwtProperties.JwtKey key : jwtProperties.getKeys()) {
            byte[] publicRaw = Base64.getDecoder().decode(key.getPublicKey());
            byte[] privateRaw = Base64.getDecoder().decode(key.getPrivateKey());
            byte[] privateBytes = Arrays.copyOfRange(privateRaw, privateRaw.length - 32, privateRaw.length);

            OctetKeyPair jwk = new OctetKeyPair.Builder(Curve.Ed25519, Base64URL.encode(publicRaw))
                    .d(Base64URL.encode(privateBytes))
                    .keyID(key.getId())
                    .build();

            jwkKeys.put(key.getId(), jwk);
        }
    }

    public String generateAccessToken(String username, String rawXsrfToken) throws JOSEException {
        OctetKeyPair signingKey = jwkKeys.get(jwtProperties.getActiveKeyId());

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                        .type(JOSEObjectType.JWT)
                        .keyID(signingKey.getKeyID())
                        .build(),
                new JWTClaimsSet.Builder()
                        .subject(username)
                        .claim("xsrf", rawXsrfToken)  // raw token here
                        .issueTime(new Date())
                        .expirationTime(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                        .build()
        );

        JWSSigner signer = new Ed25519Signer(signingKey);
        jwt.sign(signer);
        return jwt.serialize();
    }

    public SignedJWT validateToken(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        String kid = signedJWT.getHeader().getKeyID();

        OctetKeyPair baseKey = jwkKeys.get(kid);
        if (baseKey == null) throw new JOSEException("Unknown key ID: " + kid);

        JWSVerifier verifier = new Ed25519Verifier(baseKey.toPublicJWK());
        if (!signedJWT.verify(verifier)) {
            throw new JOSEException("Invalid signature");
        }

        return signedJWT;
    }

    public String getUsername(String token) throws ParseException, JOSEException {
        return validateToken(token).getJWTClaimsSet().getSubject();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }
}

