package com.renx.mg.request.controller;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.dto.LoginRequest;
import com.renx.mg.request.dto.LoginResponse;
import com.renx.mg.request.dto.MeResponse;
import com.renx.mg.request.dto.MenuItemDTO;
import com.renx.mg.request.model.ProfileMenuItem;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.ProfileMenuITemRepository;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProfileMenuITemRepository profileMenuItemRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, UserRepository userRepository,
                          CustomerRepository customerRepository,
                          ProfileMenuITemRepository profileMenuItemRepository,
                          PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.profileMenuItemRepository = profileMenuItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getUsername(), request.getPassword());
        if (response.getMessage() != null) {
            return ResponseEntity.status(401).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        String locale = user.getLocale() != null && !user.getLocale().isBlank() ? user.getLocale() : "es";
        boolean employee = user.getCustomerId() != null
                && customerRepository.findById(user.getCustomerId()).map(c -> c.isEmployee()).orElse(false);
        MeResponse resp = new MeResponse();
        resp.setUsername(user.getUsername());
        resp.setLocale(locale);
        resp.setUserId(user.getId());
        resp.setProfileId(user.getProfileId());
        resp.setEmployee(employee);
        if (Constants.COMPANY_ADMIN_PROFILE_ID.equals(user.getProfileId()) && user.getCustomerId() != null) {
            customerRepository.findById(user.getCustomerId())
                    .ifPresent(c -> resp.setCompanyId(c.getCompanyId()));
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemDTO>> menu(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null || user.getProfileId() == null) {
            return ResponseEntity.ok(List.of());
        }
        List<ProfileMenuItem> list = profileMenuItemRepository.findByProfileIdOrderByMenuItemPosition(user.getProfileId());
        List<MenuItemDTO> dtos = list.stream()
                .filter(pmi -> pmi.getMenuItem() != null)
                .map(pmi -> {
                    var m = pmi.getMenuItem();
                    MenuItemDTO dto = new MenuItemDTO();
                    dto.setId(m.getId());
                    dto.setDescription(m.getDescription());
                    dto.setUri(m.getUri());
                    dto.setPosition(m.getPosition());
                    dto.setType(m.getType());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/me/locale")
    public ResponseEntity<Void> changeMyLocale(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestBody Map<String, String> body) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        String locale = body != null ? body.get("locale") : null;
        if (locale == null || locale.isBlank() || (!locale.equals("es") && !locale.equals("en"))) {
            return ResponseEntity.badRequest().build();
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) return ResponseEntity.status(401).build();
        user.setLocale(locale);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(@AuthenticationPrincipal UserDetails userDetails,
                                                   @RequestBody Map<String, String> body) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        String currentPassword = body != null ? body.get("currentPassword") : null;
        String newPassword = body != null ? body.get("newPassword") : null;
        if (currentPassword == null || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) return ResponseEntity.status(401).build();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().build();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }
}
