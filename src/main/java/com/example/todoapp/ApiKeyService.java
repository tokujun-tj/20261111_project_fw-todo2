package com.example.todoapp;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ApiKeyService {
    private final SecureRandom secureRandom = new SecureRandom(); private final ApiKeyMapper apiKeyMapper;
    public ApiKeyService(ApiKeyMapper apiKeyMapper) { this.apiKeyMapper = apiKeyMapper; }
    @Transactional public IssuedApiKey issue(String username, String name) {
        byte[] bytes = new byte[32]; secureRandom.nextBytes(bytes);
        String rawKey = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        if (apiKeyMapper.insert(username, sha256(rawKey), name) != 1) throw new IllegalStateException("User for API key was not found");
        return new IssuedApiKey(rawKey, name);
    }
    public ApiKey findActive(String rawKey) { return apiKeyMapper.findActiveByHash(sha256(rawKey)); }
    public void markUsed(Long id) { apiKeyMapper.updateLastUsedAt(id); }
    public boolean revoke(Long id, String username) { return apiKeyMapper.revoke(id, username) == 1; }
    public List<ApiKey> findByUsername(String username) { return apiKeyMapper.findByUsername(username); }
    private String sha256(String value) { try { byte[] d = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder s = new StringBuilder(64); for (byte b : d) s.append(String.format("%02x", b)); return s.toString(); } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); } }
    public record IssuedApiKey(String apiKey, String name) {}
}
