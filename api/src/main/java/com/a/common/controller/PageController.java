package com.a.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/")
    public String feedPage() {
        return "feed";
    }

    @GetMapping("/posts/{postId}")
    public String postDetailPage() {
        return "post-detail";
    }

    @GetMapping("/notifications")
    public String notificationsPage() {
        return "notifications";
    }

    @GetMapping("/profile/{userId}")
    public String profilePage() {
        return "profile";
    }

    @GetMapping("/search")
    public String searchPage() {
        return "search";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @GetMapping("/oauth/callback/{provider}")
    public String oauthCallbackPage() {
        return "oauth-callback";
    }
}
