package com.SwiftSort.MLTicketClassifier;

import com.SwiftSort.MLTicketClassifier.model.User;
import com.SwiftSort.MLTicketClassifier.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Controller
public class ProfileController {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String profile() {
        return "layout :: layout(view='profile')";
    }

    @GetMapping("/settings")
    public String settings() {
        return "layout :: layout(view='settings')";
    }

    @PostMapping("/api/profile")
    @ResponseBody
    public void updateProfile(@RequestBody Map<String, String> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        if (body.containsKey("name")) user.setName(body.get("name"));
        userRepo.save(user);
    }

    @PostMapping("/api/profile/picture")
    @ResponseBody
    public void uploadProfilePicture(@RequestParam("file") MultipartFile file, Authentication auth) throws IOException {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        String base64 = "data:" + file.getContentType() + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
        user.setProfilePicture(base64);
        userRepo.save(user);
    }

    @PostMapping("/api/change-password")
    @ResponseBody
    public void changePassword(@RequestBody Map<String, String> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        if (!passwordEncoder.matches(body.get("currentPassword"), user.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect");
        user.setPassword(passwordEncoder.encode(body.get("newPassword")));
        userRepo.save(user);
    }
}