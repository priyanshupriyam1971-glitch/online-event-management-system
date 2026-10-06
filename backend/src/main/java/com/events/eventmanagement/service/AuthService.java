package com.events.eventmanagement.service;

import com.events.eventmanagement.dto.AuthRequest;
import com.events.eventmanagement.dto.AuthResponse;
import com.events.eventmanagement.dto.RegisterRequest;
import com.events.eventmanagement.dto.UserResponse;
import com.events.eventmanagement.entity.User;
import com.events.eventmanagement.enums.Role;
import com.events.eventmanagement.exception.BadRequestException;
import com.events.eventmanagement.repository.UserRepository;
import com.events.eventmanagement.security.JwtTokenUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthService(UserRepository userRepository,
                     PasswordEncoder passwordEncoder,
                     AuthenticationManager authenticationManager,
                     JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already registered");
        }

        Role role = request.role() != null ? request.role() : Role.ATTENDEE;
        if (role == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);
        userRepository.save(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null,
                AuthorityUtils.createAuthorityList("ROLE_" + role.name()));
        String token = jwtTokenUtil.generateToken(auth);

        return new AuthResponse(token, UserResponse.from(user));
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenUtil.generateToken(authentication);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        return new AuthResponse(token, UserResponse.from(user));
    }

    public UserResponse currentUser() {
        return UserResponse.from(loadCurrentUser());
    }

    public User loadCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Current user not found"));
    }
}
