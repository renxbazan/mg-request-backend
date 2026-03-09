package com.renx.mg.request.security;

import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        currentUserService = new CurrentUserService(userRepository, customerRepository);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_whenNoAuthentication_returnsNull() {
        User result = currentUserService.getCurrentUser();
        assertThat(result).isNull();
    }

    @Test
    void getCurrentUser_whenAnonymous_returnsNull() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null));
        User result = currentUserService.getCurrentUser();
        assertThat(result).isNull();
    }

    @Test
    void getCurrentUser_whenAuthenticated_returnsUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("joe");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("joe", null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
        when(userRepository.findByUsername("joe")).thenReturn(user);

        User result = currentUserService.getCurrentUser();

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("joe");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getCurrentUser_whenUserNotInDb_returnsNull() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("missing", null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
        lenient().when(userRepository.findByUsername("missing")).thenReturn(null);

        User result = currentUserService.getCurrentUser();

        assertThat(result).isNull();
    }
}
