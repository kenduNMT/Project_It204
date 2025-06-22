package bk.controller;

import bk.entity.Application;
import bk.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/application")
public class ApplicationManagementController {
    @Autowired
    private ApplicationService applicationService;

    private static final int PAGE_SIZE = 5;

    @GetMapping
    public String listApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Model model) {

        List<Application> applications;
        long totalItems;
        int totalPages;

        // Validate page number
        if (page < 1) page = 1;

        // Handle different scenarios based on parameters
        if (status != null && !status.isEmpty() && search != null && !search.trim().isEmpty()) {
            // Both status filter and search
            Application.Status appStatus = parseStatus(status);
            applications = applicationService.findByStatusAndSearchWithPagination(appStatus, search.trim(), page, PAGE_SIZE);
            totalItems = applicationService.countByStatusAndSearch(appStatus, search.trim());
        } else if (status != null && !status.isEmpty()) {
            // Only status filter
            Application.Status appStatus = parseStatus(status);
            applications = applicationService.findByStatusWithPagination(appStatus, page, PAGE_SIZE);
            totalItems = applicationService.countByStatus(appStatus);
        } else if (search != null && !search.trim().isEmpty()) {
            // Only search
            applications = applicationService.searchWithPagination(search.trim(), page, PAGE_SIZE);
            totalItems = applicationService.countBySearch(search.trim());
        } else {
            // No filter, show all
            applications = applicationService.findAllWithPagination(page, PAGE_SIZE);
            totalItems = applicationService.countAll();
        }

        totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        // Add attributes to model
        model.addAttribute("applications", applications);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchTerm", search);

        return "admin/application/index";
    }

    @GetMapping("/view/{id}")
    public String viewApplication(@PathVariable Long id, Model model) {
        Application app = applicationService.findById(id);
        model.addAttribute("application", app);
        return "admin/application/view";
    }

    @PostMapping("/approve/{id}")
    public String approveInterview(@PathVariable Long id,
                                   @RequestParam String interviewResult,
                                   @RequestParam String interviewResultNote) {
        Application app = applicationService.findById(id);
        app.setStatus(Application.Status.APPROVED);
        app.setInterviewResult(interviewResult);
        app.setInterviewResultNote(interviewResultNote);
        applicationService.update(app);
        return "redirect:/admin/application";
    }

    @PostMapping("/destroy/{id}")
    public String destroyInterview(@PathVariable Long id, @RequestParam String destroyReason) {
        Application app = applicationService.findById(id);
        app.setStatus(Application.Status.DESTROYED);
        app.setDestroyReason(destroyReason);
        applicationService.update(app);
        return "redirect:/admin/application";
    }

    @PostMapping("/interview/{id}")
    public String scheduleInterview(@PathVariable Long id,
                                    @RequestParam String interviewLink,
                                    @RequestParam String interviewTime) {
        Application app = applicationService.findById(id);
        app.setStatus(Application.Status.INTERVIEWING);
        // app.setInterviewLink(interviewLink); // Nếu có trường này
        // app.setInterviewTime(LocalDateTime.parse(interviewTime));
        applicationService.update(app);
        return "redirect:/admin/application";
    }

    @PostMapping("/delete/{id}")
    public String deleteApplication(@PathVariable Long id) {
        applicationService.delete(id);
        return "redirect:/admin/application";
    }

    private Application.Status parseStatus(String status) {
        try {
            return Application.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Application.Status.PENDING; // Default status
        }
    }
}