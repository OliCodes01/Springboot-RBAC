package com.jwtapp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Utility class for JWT token generation and validation using RS256 algorithm.
 * Loads RSA keys from PEM files on the classpath and caches them in memory.
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.expiration}")
    private long expiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    void init() {
        this.privateKey = loadPrivateKey(Constants.PRIVATE_KEY_PATH);
        this.publicKey = loadPublicKey(Constants.PUBLIC_KEY_PATH);
        log.info("RSA key pair loaded successfully");
    }

    public String generateToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public long getExpirationSeconds() {
        return Math.max(0, expiration / 1000);
    }

    public UUID extractUserId(String token) {
        Claims claims = extractClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PrivateKey loadPrivateKey(String resourcePath) {
        try {
            byte[] keyBytes = readPemBytes(resourcePath);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key from " + resourcePath, e);
        }
    }

    private PublicKey loadPublicKey(String resourcePath) {
        try {
            byte[] keyBytes = readPemBytes(resourcePath);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load public key from " + resourcePath, e);
        }
    }

    private byte[] readPemBytes(String resourcePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        String pemContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String base64Content = pemContent
                .replaceAll("-----[A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64Content);
    }
}
