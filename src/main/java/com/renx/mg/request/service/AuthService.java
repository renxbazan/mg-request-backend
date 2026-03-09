package com.renx.mg.request.service;

import com.renx.mg.request.dto.LoginResponse;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            return LoginResponse.error("Usuario es requerido");
        }
        User user = userRepository.findByUsername(username.trim().toLowerCase());
        if (user == null) {
            return LoginResponse.error("Credenciales inválidas");
        }
        String storedPassword = user.getPassword();
        boolean passwordMatches = passwordEncoder.matches(rawPassword, storedPassword);
        if (!passwordMatches && storedPassword != null && storedPassword.equals(rawPassword)) {
            passwordMatches = true;
        }
        if (!passwordMatches) {
            return LoginResponse.error("Credenciales inválidas");
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        String locale = user.getLocale() != null && !user.getLocale().isBlank() ? user.getLocale() : "es";
        return new LoginResponse(token, user.getUsername(), user.getId(), user.getProfileId(), locale);
    }
}
