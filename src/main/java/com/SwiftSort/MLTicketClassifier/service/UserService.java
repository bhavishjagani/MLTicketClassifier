package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.dto.UserCreateRequest;
import com.SwiftSort.MLTicketClassifier.dto.UserDto;
import com.SwiftSort.MLTicketClassifier.dto.UserResponse;
import com.SwiftSort.MLTicketClassifier.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {
    List<UserDto> findAll();
    List<UserDto> findByRole(String role);
    void toggleActive(Long id);
    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void resetPassword(Long id);
    void deleteUser(Long id);
}