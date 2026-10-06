package com.events.eventmanagement.controller;

import com.events.eventmanagement.dto.*;
import com.events.eventmanagement.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return adminService.listUsers();
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return adminService.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return adminService.getStats();
    }

    // ---------- Registration approval ----------

    @GetMapping("/registrations")
    public List<RegistrationResponse> listRegistrations(
            @RequestParam(required = false) String status) {
        return adminService.listRegistrations(status);
    }

    @PostMapping("/registrations/{id}/approve")
    public RegistrationResponse approveRegistration(@PathVariable Long id) {
        return adminService.approveRegistration(id);
    }

    @PostMapping("/registrations/{id}/cancel")
    public RegistrationResponse cancelRegistration(@PathVariable Long id) {
        return adminService.cancelRegistration(id);
    }

    @GetMapping("/activities")
    public List<ActivityLogResponse> activities() {
        return adminService.listActivities();
    }
}
