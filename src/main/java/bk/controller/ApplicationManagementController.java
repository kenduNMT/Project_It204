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

    @GetMapping
    public String listApplications(Model model) {
        List<Application> applications = applicationService.findAll();
        model.addAttribute("applications", applications);
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
}