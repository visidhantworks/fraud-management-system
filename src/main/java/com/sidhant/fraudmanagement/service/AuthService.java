package com.sidhant.fraudmanagement.service;

import com.sidhant.fraudmanagement.dto.request.LoginRequest;
import com.sidhant.fraudmanagement.dto.response.LoginResponse;
import com.sidhant.fraudmanagement.entity.User;
import com.sidhant.fraudmanagement.entity.ActiveSession;
import com.sidhant.fraudmanagement.exception.ActiveSessionException;
import com.sidhant.fraudmanagement.exception.UserNotFoundException;
import com.sidhant.fraudmanagement.repository.UserRepository;
import com.sidhant.fraudmanagement.repository.ActiveSessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ActiveSessionRepository activeSessionRepository;
    public AuthService(UserRepository userRepository,PasswordEncoder passwordEncoder, JwtService jwtService , ActiveSessionRepository activeSessionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.activeSessionRepository = activeSessionRepository;
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
        if(activeSessionRepository.findByUser_IdAndActiveTrue(user.getId()).isPresent()){
            throw new ActiveSessionException("User Already has an active session!!");
        }
        String token  = jwtService.generateToken(user);
        String sessionId = java.util.UUID.randomUUID().toString();

        ActiveSession activeSession = activeSessionRepository
                .findByUser_Id(user.getId())
                .orElseGet(() -> new ActiveSession(user, sessionId));
        LocalDateTime now = LocalDateTime.now();
        activeSession.setActive(true);
        activeSession.setSessionId(sessionId);
        activeSession.setLoginAt(java.time.LocalDateTime.now());
        activeSession.setLogoutAt(null);
        activeSession.setLastActivityAt(now);
        activeSessionRepository.save(activeSession);
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token
        );
    }
    public void logout(String email) {

    User user = userRepository
            .findByEmail(email)
            .orElseThrow(() ->
                    new UserNotFoundException("User not found")
            );

    ActiveSession activeSession = activeSessionRepository
            .findByUser_IdAndActiveTrue(user.getId())
            .orElseThrow(() ->
                    new IllegalStateException("No active session found")
            );

    activeSession.setActive(false);
    activeSession.setLogoutAt(java.time.LocalDateTime.now());

    activeSessionRepository.save(activeSession);
}
}