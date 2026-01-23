package com.teklif.app.service;

import com.teklif.app.dto.request.LoginRequest;
import com.teklif.app.dto.response.LoginResponse;
import com.teklif.app.dto.response.UserResponse;
import com.teklif.app.entity.User;
import com.teklif.app.exception.CustomException;
import com.teklif.app.mapper.UserMapper;
import com.teklif.app.repository.UserRepository;
import com.teklif.app.security.CustomUserDetails;
import com.teklif.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // Update last login
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Generate token
        String token = jwtUtil.generateToken(
                userDetails,
                user.getId(),
                user.getTenantId(),
                user.getRole().name()
        );

        UserResponse userResponse = userMapper.toResponse(user);

        return LoginResponse.builder()
                .user(userResponse)
                .token(token)
                .build();
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw CustomException.unauthorized("User not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByIdAndIsDeletedFalse(userDetails.getUserId())
                .orElseThrow(() -> CustomException.notFound("User not found"));

        return userMapper.toResponse(user);
    }
}