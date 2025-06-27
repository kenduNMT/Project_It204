package bk.controller;

import bk.entity.Candidate;
import bk.service.ApplicationService;
import bk.service.CandidateService;
import bk.service.RecruitmentPositionService;
import bk.service.TechnologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private RecruitmentPositionService recruitmentPositionService;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model, HttpSession session) {
        Candidate loggedInUser = (Candidate) session.getAttribute("currentCandidate");

        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        if (loggedInUser.getRole() != Candidate.Role.ADMIN) {
            return "redirect:/";
        }

        // Lấy tổng số từ các service
        long totalCandidates = candidateService.getTotalCount();
        long totalTechnologies = technologyService.getTotalCount();
        long totalRecruitmentPositions = recruitmentPositionService.getTotalCount();
        long totalApplications = applicationService.getTotalCount();

        // Thêm các thống kê vào model
        model.addAttribute("admin", loggedInUser);
        model.addAttribute("totalCandidates", totalCandidates);
        model.addAttribute("totalTechnologies", totalTechnologies);
        model.addAttribute("totalRecruitmentPositions", totalRecruitmentPositions);
        model.addAttribute("totalApplications", totalApplications);

        return "/admin/dashboard";
    }
}