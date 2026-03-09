package com.renx.mg.request.service;

import com.renx.mg.request.dto.LoginResponse;
import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtUtil, passwordEncoder);
    }

    @Test
    void login_whenUserNotFound_returnsError() {
        when(userRepository.findByUsername("unknown")).thenReturn(null);

        LoginResponse response = authService.login("unknown", "pass");

        assertThat(response.getMessage()).isEqualTo("Credenciales inválidas");
        assertThat(response.getToken()).isNull();
    }

    @Test
    void login_whenPasswordDoesNotMatch_returnsError() {
        User user = createUser(1L, "user1", "$2a$10$encoded");
        when(userRepository.findByUsername("user1")).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginResponse response = authService.login("user1", "wrong");

        assertThat(response.getMessage()).isEqualTo("Credenciales inválidas");
    }

    @Test
    void login_whenCredentialsValid_returnsTokenAndDefaultLocale() {
        User user = createUser(1L, "user1", "$2a$10$encoded");
        when(userRepository.findByUsername("user1")).thenReturn(user);
        when(passwordEncoder.matches("pass", "$2a$10$encoded")).thenReturn(true);
        when(jwtUtil.generateToken("user1", 1L)).thenReturn("jwt.token.here");

        LoginResponse response = authService.login("user1", "pass");

        assertThat(response.getMessage()).isNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getUsername()).isEqualTo("user1");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getLocale()).isEqualTo("es");
        verify(jwtUtil).generateToken("user1", 1L);
    }

    @Test
    void login_whenUserHasLocale_returnsThatLocale() {
        User user = createUser(1L, "user1", "$2a$10$encoded");
        user.setLocale("en");
        when(userRepository.findByUsername("user1")).thenReturn(user);
        when(passwordEncoder.matches("pass", "$2a$10$encoded")).thenReturn(true);
        when(jwtUtil.generateToken("user1", 1L)).thenReturn("jwt.token.here");

        LoginResponse response = authService.login("user1", "pass");

        assertThat(response.getLocale()).isEqualTo("en");
    }

    @Test
    void login_withBlankUsername_returnsError() {
        LoginResponse response = authService.login("", "pass");
        assertThat(response.getMessage()).isNotNull();
    }

    private static User createUser(Long id, String username, String encodedPassword) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setProfileId(2L);
        return user;
    }
}
