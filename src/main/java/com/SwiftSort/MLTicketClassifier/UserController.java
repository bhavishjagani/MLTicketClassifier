package com.SwiftSort.MLTicketClassifier;

import com.SwiftSort.MLTicketClassifier.dto.PasswordChangeRequest;
import com.SwiftSort.MLTicketClassifier.dto.UserCreateRequest;
import com.SwiftSort.MLTicketClassifier.dto.UserDto;
import com.SwiftSort.MLTicketClassifier.dto.UserResponse;
import com.SwiftSort.MLTicketClassifier.dto.UserUpdateRequest;
import com.SwiftSort.MLTicketClassifier.model.User;
import com.SwiftSort.MLTicketClassifier.repository.UserRepository;
import com.SwiftSort.MLTicketClassifier.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {
    private final UserService userService;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public String usersPage() {
        return "layout :: layout(view='users')";
    }

    @GetMapping("/api/users")
    @ResponseBody
    public List<UserDto> getUsers() {
        return userService.findAll();
    }

    @GetMapping("/api/users/agents")
    @ResponseBody
    public List<UserDto> getAgents() {
        return userService.findByRole("AGENT");
    }

    @PostMapping("/api/users")
    @ResponseBody
    public UserResponse createUser(@RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }

    @PatchMapping("/api/users/{id}")
    @ResponseBody
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    @PatchMapping("/api/users/{id}/toggle")
    @ResponseBody
    public void toggleUser(@PathVariable Long id) {
        userService.toggleActive(id);
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PatchMapping("/api/users/{id}/reset-password")
    @ResponseBody
    public void resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
    }

    @PatchMapping("/api/users/me/password")
    @ResponseBody
    public void changePassword(Authentication auth, @RequestBody PasswordChangeRequest request) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
    }

    @GetMapping("/api/users/me")
    @ResponseBody
    public UserDto getCurrentUser(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setActive(user.isActive());
        dto.setProfilePicture(user.getProfilePicture());
        return dto;
    }
}