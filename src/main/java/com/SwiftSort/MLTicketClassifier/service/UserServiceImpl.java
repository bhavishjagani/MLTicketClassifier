package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.dto.UserCreateRequest;
import com.SwiftSort.MLTicketClassifier.dto.UserDto;
import com.SwiftSort.MLTicketClassifier.dto.UserResponse;
import com.SwiftSort.MLTicketClassifier.dto.UserUpdateRequest;
import com.SwiftSort.MLTicketClassifier.model.User;
import com.SwiftSort.MLTicketClassifier.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepo.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findByRole(String role) {
        User.Role r = User.Role.valueOf(role.toUpperCase());
        return userRepo.findByRoleAndActiveTrue(r).stream().map(this::toDto).toList();
    }

    @Override
    public void toggleActive(Long id) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        u.setActive(!u.isActive());
        userRepo.save(u);
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : User.Role.VIEWER);
        user.setActive(true);
        user = userRepo.save(user);
        return toResponse(user);
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        user = userRepo.save(user);
        return toResponse(user);
    }

    @Override
    public void resetPassword(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        user.setPassword(passwordEncoder.encode("changeme"));
        userRepo.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        userRepo.delete(user);
    }

    private UserDto toDto(User u) {
        UserDto d = new UserDto();
        d.setId(u.getId());
        d.setName(u.getName());
        d.setEmail(u.getEmail());
        d.setRole(u.getRole().name());
        d.setActive(u.isActive());
        return d;
    }

    private UserResponse toResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setName(u.getName());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole());
        r.setActive(u.isActive());
        return r;
    }
}