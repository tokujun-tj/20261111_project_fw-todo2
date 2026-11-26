package com.example.todoapp;
import java.io.IOException; import java.time.Duration; import java.time.Instant; import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.*; import jakarta.servlet.http.*;
import org.springframework.http.*; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.userdetails.UserDetailsService; import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; import org.springframework.web.filter.OncePerRequestFilter;
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private static final int MAX = 1000; private static final Duration WINDOW = Duration.ofHours(1);
    private final ApiKeyService service; private final UserDetailsService users; private final ConcurrentHashMap<String, Window> limits = new ConcurrentHashMap<>();
    public ApiKeyAuthenticationFilter(ApiKeyService service, UserDetailsService users) { this.service = service; this.users = users; }
    @Override protected boolean shouldNotFilter(HttpServletRequest r) { String p = r.getContextPath() + "/api/"; return !r.getRequestURI().startsWith(p) || ("POST".equals(r.getMethod()) && r.getRequestURI().equals(r.getContextPath() + "/api/keys")); }
    @Override protected void doFilterInternal(HttpServletRequest r, HttpServletResponse p, FilterChain chain) throws ServletException, IOException {
        // The web UI calls the same API endpoints with the authenticated session.
        // API keys are only required for requests that do not already have a
        // logged-in user (for example, external API clients).
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            chain.doFilter(r, p);
            return;
        }
        String raw = r.getHeader("X-API-Key"); if (raw == null || raw.isBlank() || raw.length() > 200) { unauthorized(p, "X-API-Key header is required"); return; }
        ApiKey key = service.findActive(raw); if (key == null) { unauthorized(p, "Invalid or revoked API key"); return; }
        Window w = limits.computeIfAbsent(key.getKeyHash(), x -> new Window()); synchronized (w) { if (Duration.between(w.started, Instant.now()).compareTo(WINDOW) >= 0) { w.started = Instant.now(); w.count = 0; } if (++w.count > MAX) { p.setStatus(429); p.setContentType(MediaType.APPLICATION_JSON_VALUE); p.getWriter().write("{\"error\":\"API rate limit exceeded\"}"); return; } }
        var user = users.loadUserByUsername(key.getUsername()); var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()); auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(r)); org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth); service.markUsed(key.getId()); chain.doFilter(r, p);
    }
    private void unauthorized(HttpServletResponse p, String message) throws IOException { p.setStatus(401); p.setHeader(HttpHeaders.WWW_AUTHENTICATE, "ApiKey"); p.setContentType(MediaType.APPLICATION_JSON_VALUE); p.getWriter().write("{\"error\":\"" + message + "\"}"); }
    private static class Window { Instant started = Instant.now(); int count; }
}
