package bk.controller;

import bk.entity.Candidate;
import bk.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/candidate-list")
public class CandidateManagementController {

    @Autowired
    private CandidateService candidateService;

    @GetMapping
    public String listCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gender,
            Model model,
            HttpSession session) {

        // Admin access check
        Candidate loggedInUser = (Candidate) session.getAttribute("currentCandidate");
        if (loggedInUser == null || loggedInUser.getRole() != Candidate.Role.ADMIN) {
            return "redirect:/auth/login";
        }

        // Create sort object
        Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Candidate> candidatePage;

        // Apply filters
        if ((search != null && !search.trim().isEmpty()) ||
                (status != null && !status.isEmpty()) ||
                (gender != null && !gender.isEmpty())) {
            candidatePage = candidateService.searchCandidates(search, status, gender, pageable);
        } else {
            candidatePage = candidateService.getAllCandidates(pageable);
        }

        // Add attributes to model
        model.addAttribute("candidates", candidatePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", candidatePage.getTotalPages());
        model.addAttribute("totalElements", candidatePage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("gender", gender);
        model.addAttribute("admin", loggedInUser);

        return "admin/candidate-list"; // Corresponds to your first HTML template
    }

    @GetMapping("/{id}")
    public String viewCandidateDetail(
            @PathVariable("id") Long id,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Admin access check
        Candidate loggedInUser = (Candidate) session.getAttribute("currentCandidate");
        if (loggedInUser == null || loggedInUser.getRole() != Candidate.Role.ADMIN) {
            return "redirect:/auth/login";
        }

        Candidate candidate = candidateService.getCandidateById(id);
        if (candidate == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy candidate");
            return "redirect:/admin/candidate-list";
        }

        model.addAttribute("candidate", candidate);
        model.addAttribute("admin", loggedInUser);

        return "admin/candidate-detail"; // Corresponds to your second HTML template
    }

    @PostMapping("/{id}/toggle-status")
    @ResponseBody
    public String toggleCandidateStatus(
            @PathVariable("id") Long id,
            HttpSession session) {

        Candidate loggedInUser = (Candidate) session.getAttribute("currentCandidate");
        if (loggedInUser == null || loggedInUser.getRole() != Candidate.Role.ADMIN) {
            return "unauthorized";
        }

        try {
            Candidate candidate = candidateService.getCandidateById(id);
            if (candidate != null) {
                if (candidate.getStatus() == Candidate.Status.ACTIVE) {
                    candidate.setStatus(Candidate.Status.INACTIVE);
                } else {
                    candidate.setStatus(Candidate.Status.ACTIVE);
                }
                candidateService.updateCandidate(candidate);
                return "success";
            } else {
                return "not_found";
            }
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCandidate(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Candidate loggedInUser = (Candidate) session.getAttribute("currentCandidate");
        if (loggedInUser == null || loggedInUser.getRole() != Candidate.Role.ADMIN) {
            return "redirect:/auth/login";
        }

        try {
            Candidate candidate = candidateService.getCandidateById(id);
            if (candidate != null) {
                candidateService.deleteCandidate(id);
                redirectAttributes.addFlashAttribute("success", "Đã xóa candidate " + candidate.getName() + " thành công");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy candidate");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa candidate");
        }

        return "redirect:/admin/candidate-list";
    }
}