package com.sidhant.fraudmanagement.service;

import com.sidhant.fraudmanagement.dto.request.LoginRequest;
import com.sidhant.fraudmanagement.dto.response.LoginResponse;
import com.sidhant.fraudmanagement.entity.User;
import com.sidhant.fraudmanagement.exception.UserNotFoundException;
import com.sidhant.fraudmanagement.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException("Invalid email or password")
                );

        boolean passwordValid = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordValid) {
            throw new UserNotFoundException("Invalid email or password");
        }
        String token  = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token
        );
    }
}