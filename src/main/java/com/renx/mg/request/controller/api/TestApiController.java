package com.renx.mg.request.controller.api;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.model.User;
import com.renx.mg.request.security.CurrentUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints SOLO para entorno de test (E2E) para limpiar datos por prefijo.
 */
@Profile("test")
@RestController
@RequestMapping("/api/test")
public class TestApiController {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUserService currentUserService;

    public TestApiController(JdbcTemplate jdbcTemplate, CurrentUserService currentUserService) {
        this.jdbcTemplate = jdbcTemplate;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/cleanup")
    @Transactional
    public ResponseEntity<Map<String, Object>> cleanup(@RequestBody Map<String, String> body) {
        User current = currentUserService.getCurrentUser();
        if (current == null || !Constants.SUPER_ADMIN_PROFILE_ID.equals(current.getProfileId())) {
            return ResponseEntity.status(403).body(Map.of("message", "FORBIDDEN"));
        }
        String prefix = body != null ? body.get("prefix") : null;
        if (prefix == null || prefix.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "prefix is required"));
        }
        // MySQL LIKE pattern
        String like = prefix + "%";

        // Orden: hijos -> padres (por FK).
        int rh = jdbcTemplate.update(
                "DELETE rh FROM request_history rh JOIN request r ON r.id = rh.request_id WHERE r.description LIKE ?",
                like
        );
        int ra = jdbcTemplate.update(
                "DELETE ra FROM request_assignment ra JOIN request r ON r.id = ra.request_id WHERE r.description LIKE ?",
                like
        );
        int r = jdbcTemplate.update("DELETE FROM request WHERE description LIKE ?", like);

        int u = jdbcTemplate.update("DELETE FROM users WHERE username LIKE ?", like);
        int c = jdbcTemplate.update("DELETE FROM customer WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ?", like, like, like);

        int s = jdbcTemplate.update("DELETE FROM site WHERE name LIKE ?", like);

        int ssc = jdbcTemplate.update("DELETE FROM service_sub_category WHERE name LIKE ?", like);
        int sc = jdbcTemplate.update("DELETE FROM service_category WHERE name LIKE ?", like);

        int co = jdbcTemplate.update("DELETE FROM company WHERE name LIKE ?", like);

        return ResponseEntity.ok(Map.of(
                "prefix", prefix,
                "deleted", Map.of(
                        "request_history", rh,
                        "request_assignment", ra,
                        "request", r,
                        "users", u,
                        "customer", c,
                        "site", s,
                        "service_sub_category", ssc,
                        "service_category", sc,
                        "company", co
                )
        ));
    }
}

