package com.SwiftSort.MLTicketClassifier;

import com.SwiftSort.MLTicketClassifier.dto.RegisterRequest;
import com.SwiftSort.MLTicketClassifier.dto.TicketCreateRequest;
import com.SwiftSort.MLTicketClassifier.dto.TicketResponse;
import com.SwiftSort.MLTicketClassifier.model.PasswordResetToken;
import com.SwiftSort.MLTicketClassifier.model.User;
import com.SwiftSort.MLTicketClassifier.repository.PasswordResetTokenRepository;
import com.SwiftSort.MLTicketClassifier.repository.UserRepository;
import com.SwiftSort.MLTicketClassifier.security.JwtService;
import com.SwiftSort.MLTicketClassifier.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository resetTokenRepo;
    private final AuthenticationManager authenticationManager;
    private final TicketService ticketService;

    public AuthApiController(UserRepository userRepo, PasswordEncoder encoder, JwtService jwtService,
                             PasswordResetTokenRepository resetTokenRepo, AuthenticationManager authenticationManager,
                             TicketService ticketService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.resetTokenRepo = resetTokenRepo;
        this.authenticationManager = authenticationManager;
        this.ticketService = ticketService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest req) {
        if (req.getName() == null || req.getName().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Name is required"));
        if (req.getEmail() == null || req.getEmail().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        if (req.getPassword() == null || req.getPassword().length() < 8)
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 8 characters"));
        if (userRepo.findByEmail(req.getEmail().trim().toLowerCase()).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "An account with this email already exists"));

        User user = new User();
        user.setName(req.getName().trim());
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(User.Role.AGENT);
        user.setActive(true);
        userRepo.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Account created"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = userRepo.findByEmail(email).orElseThrow();
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || !jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid refresh token"));
        }
        String email = jwtService.extractEmail(refreshToken);
        User user = userRepo.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "accessToken", jwtService.generateAccessToken(user.getEmail(), user.getRole().name()),
                "refreshToken", jwtService.generateRefreshToken(user.getEmail())
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        var userOpt = userRepo.findByEmail(email != null ? email.trim().toLowerCase() : "");
        if (userOpt.isPresent()) {
            PasswordResetToken token = new PasswordResetToken();
            token.setToken(UUID.randomUUID().toString());
            token.setUser(userOpt.get());
            token.setExpiresAt(LocalDateTime.now().plusHours(24));
            resetTokenRepo.save(token);
        }
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 8 characters"));
        }
        PasswordResetToken resetToken = resetTokenRepo.findByToken(token).orElse(null);
        if (resetToken == null || resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
        }
        User user = resetToken.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
        resetToken.setUsed(true);
        resetTokenRepo.save(resetToken);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @PostMapping("/public/tickets")
    public ResponseEntity<TicketResponse> publicCreateTicket(@RequestBody TicketCreateRequest request) {
        if (request.getSource() == null || request.getSource().isBlank()) {
            request.setSource("CONTACT_FORM");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request));
    }
}
