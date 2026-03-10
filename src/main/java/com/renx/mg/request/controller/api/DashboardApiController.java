package com.renx.mg.request.controller.api;

import com.renx.mg.request.model.User;
import com.renx.mg.request.security.CurrentUserService;
import com.renx.mg.request.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    public DashboardApiController(DashboardService dashboardService, CurrentUserService currentUserService) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam(required = false, defaultValue = "this_month") String range) {
        User current = currentUserService.getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(403).build();
        }
        Map<String, Object> stats = dashboardService.getStats(current, range);
        return ResponseEntity.ok(stats);
    }
}
