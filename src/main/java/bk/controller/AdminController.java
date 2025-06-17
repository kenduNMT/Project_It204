package bk.controller;

import bk.entity.Candidate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model, HttpSession session) {
        Candidate loggedInUser = (Candidate) session.getAttribute("currentCandidate");

        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        if (loggedInUser.getRole() != Candidate.Role.ADMIN) {
            return "redirect:/";
        }

        model.addAttribute("admin", loggedInUser);
        return "/admin/dashboard";
    }
}