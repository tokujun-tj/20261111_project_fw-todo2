package com.example.todoapp;
import java.time.LocalDateTime; import java.util.Map; import jakarta.validation.Valid; import jakarta.validation.constraints.Size; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/keys")
public class ApiKeyController {
    private final ApiKeyService service; public ApiKeyController(ApiKeyService service) { this.service = service; }
    @PostMapping public ResponseEntity<?> issue(@Valid @RequestBody(required=false) IssueRequest request, Authentication authentication) { String name = request == null || request.name() == null || request.name().isBlank() ? "default" : request.name().trim(); var key = service.issue(authentication.getName(), name); return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("apiKey", key.apiKey(), "name", key.name(), "issuedAt", LocalDateTime.now(), "warning", "This is the only time the full API key is displayed.")); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> revoke(@PathVariable Long id, Authentication authentication) { return service.revoke(id, authentication.getName()) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build(); }
    public record IssueRequest(@Size(max=100) String name) {}
}
