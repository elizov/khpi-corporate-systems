package com.shop.admin.controller;

import com.shop.admin.service.OrderDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final OrderDashboardService dashboardService;

    public DashboardController(OrderDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("snapshot", dashboardService.loadSnapshot());
        return "dashboard";
    }
}
