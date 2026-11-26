package com.example.todoapp;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings/api-keys")
public class ApiKeySettingsController {
    private final ApiKeyService apiKeyService;

    public ApiKeySettingsController(ApiKeyService apiKeyService) { this.apiKeyService = apiKeyService; }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        model.addAttribute("apiKeys", apiKeyService.findByUsername(authentication.getName()));
        return "api-keys";
    }

    @PostMapping
    public String issue(@Valid @ModelAttribute IssueForm form, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        String name = form.name() == null || form.name().isBlank() ? "default" : form.name().trim();
        redirectAttributes.addFlashAttribute("issuedApiKey",
                apiKeyService.issue(authentication.getName(), name).apiKey());
        redirectAttributes.addFlashAttribute("issuedApiKeyName", name);
        return "redirect:/settings/api-keys";
    }

    @PostMapping("/{id}/revoke")
    public String revoke(@PathVariable Long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (apiKeyService.revoke(id, authentication.getName())) {
            redirectAttributes.addFlashAttribute("message", "APIキーを無効化しました。");
        }
        return "redirect:/settings/api-keys";
    }

    public record IssueForm(@Size(max = 100) String name) {}
}
