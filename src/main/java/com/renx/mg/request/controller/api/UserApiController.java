package com.renx.mg.request.controller.api;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.dto.UserDTO;
import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public UserApiController(UserRepository userRepository,
                             CustomerRepository customerRepository,
                             CurrentUserService currentUserService,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/workers")
    public List<UserDTO> workers() {
        return customerRepository.findByEmployee(true).stream()
                .map(c -> userRepository.findByCustomerId(c.getId()))
                .filter(u -> u != null)
                .map(DtoMapper::toDto)
                .toList();
    }

    @GetMapping
    public List<UserDTO> list(@RequestParam(required = false) Long companyId) {
        Long restrictedCompanyId = currentUserService.getCurrentUserCompanyIdIfCompanyAdmin();
        if (restrictedCompanyId != null) {
            companyId = restrictedCompanyId;
        }
        List<User> list;
        if (companyId != null) {
            list = userRepository.findByCustomer_CompanyId(companyId);
        } else {
            list = StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();
        }
        return list.stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> get(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        Long allowedCompanyId = currentUserService.getCurrentUserCompanyIdIfCompanyAdmin();
        if (allowedCompanyId != null) {
            Customer c = user.getCustomerId() != null ? customerRepository.findById(user.getCustomerId()).orElse(null) : null;
            if (c == null || !allowedCompanyId.equals(c.getCompanyId())) {
                return forbiddenUser();
            }
        }
        return ResponseEntity.ok(DtoMapper.toDto(user));
    }

    private static ResponseEntity<UserDTO> forbiddenUser() {
        return ResponseEntity.status(403).body((UserDTO) null);
    }

    private static ResponseEntity<Void> forbiddenVoid() {
        return ResponseEntity.status(403).body((Void) null);
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Long allowedCompanyId = currentUserService.getCurrentUserCompanyIdIfCompanyAdmin();
        if (allowedCompanyId != null) {
            if (user.getCustomerId() == null) {
                return forbiddenUser();
            }
            Customer customer = customerRepository.findById(user.getCustomerId()).orElse(null);
            if (customer == null || !allowedCompanyId.equals(customer.getCompanyId())) {
                return forbiddenUser();
            }
        }
        user.setUsername(user.getUsername().trim().toLowerCase());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        User saved = userRepository.save(user);
        return ResponseEntity.ok(DtoMapper.toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        Long allowedCompanyId = currentUserService.getCurrentUserCompanyIdIfCompanyAdmin();
        if (allowedCompanyId != null) {
            Customer c = user.getCustomerId() != null ? customerRepository.findById(user.getCustomerId()).orElse(null) : null;
            if (c == null || !allowedCompanyId.equals(c.getCompanyId())) {
                return forbiddenVoid();
            }
        }
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @RequestBody User body) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        Long allowedCompanyId = currentUserService.getCurrentUserCompanyIdIfCompanyAdmin();
        if (allowedCompanyId != null) {
            Customer existingCustomer = existing.getCustomerId() != null ? customerRepository.findById(existing.getCustomerId()).orElse(null) : null;
            if (existingCustomer == null || !allowedCompanyId.equals(existingCustomer.getCompanyId())) {
                return forbiddenUser();
            }
            if (body.getCustomerId() != null) {
                Customer newCustomer = customerRepository.findById(body.getCustomerId()).orElse(null);
                if (newCustomer == null || !allowedCompanyId.equals(newCustomer.getCompanyId())) {
                    return forbiddenUser();
                }
            }
        }
        if (body.getCustomerId() != null) existing.setCustomerId(body.getCustomerId());
        if (body.getProfileId() != null) existing.setProfileId(body.getProfileId());
        existing.setSiteId(body.getSiteId());
        if (body.getLocale() != null && (body.getLocale().equals("es") || body.getLocale().equals("en"))) {
            existing.setLocale(body.getLocale());
        }
        return ResponseEntity.ok(DtoMapper.toDto(userRepository.save(existing)));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User current = currentUserService.getCurrentUser();
        if (current == null) return ResponseEntity.status(403).build();
        if (!Constants.SUPER_ADMIN_PROFILE_ID.equals(current.getProfileId())) {
            return ResponseEntity.status(403).build();
        }
        String newPassword = body != null ? body.get("newPassword") : null;
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return userRepository.findById(id)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
