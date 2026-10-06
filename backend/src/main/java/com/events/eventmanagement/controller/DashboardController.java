package com.events.eventmanagement.controller;

import com.events.eventmanagement.dto.OrganizerStatsResponse;
import com.events.eventmanagement.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizers")
@PreAuthorize("hasRole('ORGANIZER')")
public class DashboardController {

    private final AdminService adminService;

    public DashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public OrganizerStatsResponse organizerStats() {
        return adminService.getOrganizerStats();
    }
}
