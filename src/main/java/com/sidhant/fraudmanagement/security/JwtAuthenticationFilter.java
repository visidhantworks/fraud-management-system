package com.sidhant.fraudmanagement.security;
import jakarta.servlet.http.HttpServletResponse;
import com.sidhant.fraudmanagement.service.JwtService;
import com.sidhant.fraudmanagement.entity.ActiveSession;
import com.sidhant.fraudmanagement.entity.User;
import com.sidhant.fraudmanagement.repository.ActiveSessionRepository;
import com.sidhant.fraudmanagement.repository.UserRepository;
import com.sidhant.fraudmanagement.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ActiveSessionRepository activeSessionRepository;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            ActiveSessionRepository activeSessionRepository,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.activeSessionRepository = activeSessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);


        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null){
                filterChain.doFilter(request , response);
                return;
        }
        ActiveSession activeSession = activeSessionRepository.findByUser_IdAndActiveTrue(user.getId()).orElse(null);
        if (activeSession == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"code\":\"SESSION_INVALID\",\"error\":\"Your session is no longer active. Please sign in again.\"}"
        );
        return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (activeSession.getLastActivityAt() == null || activeSession.getLastActivityAt().plusMinutes(30).isBefore(now)) {

        activeSession.setActive(false);
        activeSession.setLogoutAt(now);
        activeSessionRepository.save(activeSession);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"code\":\"SESSION_EXPIRED\",\"error\":\"Your session has expired. Please sign in again.\"}"
        );
        return;
        }
        activeSession.setLastActivityAt(now);
        activeSessionRepository.save(activeSession);

 
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}