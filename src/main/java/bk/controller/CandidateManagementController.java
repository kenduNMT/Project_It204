package bk.controller;

import bk.entity.Candidate;
import bk.entity.Technology;
import bk.service.CandidateService;
import bk.service.TechnologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin/candidate-list")
public class CandidateManagementController {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private TechnologyService technologyService;

    @GetMapping
    public String listCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String technologyId,
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

        Page<Candidate> candidatePage = candidateService.searchCandidates(
                search,
                experience,
                gender,
                technologyId,
                pageable
        );

        // Get all active technologies for the dropdown
        List<Technology> allTechnologies = technologyService.findAllActive();

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
        model.addAttribute("allTechnologies", allTechnologies);

        return "admin/candidate-list";
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

        return "admin/candidate-detail";
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

        // Fix: Prevent admin from deactivating themselves
        if (loggedInUser.getId().equals(id)) {
            return "cannot_deactivate_self";
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

        // Fix: Prevent admin from deleting themselves
        if (loggedInUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không thể xóa tài khoản của chính mình");
            return "redirect:/admin/candidate-list";
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

    @PostMapping("/{candidateId}/add-technology/{techId}")
    @ResponseBody
    public ResponseEntity<?> addTechnologyToCandidate(
            @PathVariable Long candidateId,
            @PathVariable Integer techId,
            HttpSession session) {

        // Admin access check
        Candidate loggedInUser = (Candidate) session.getAttribute("currentCandidate");
        if (loggedInUser == null || loggedInUser.getRole() != Candidate.Role.ADMIN) {
            return ResponseEntity.status(401).body("unauthorized");
        }

        try {
            boolean success = candidateService.addTechnologyToCandidate(candidateId, techId);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("not_found");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("error");
        }
    }

    @PostMapping("/{candidateId}/remove-technology/{techId}")
    @ResponseBody
    public ResponseEntity<?> removeTechnologyFromCandidate(
            @PathVariable Long candidateId,
            @PathVariable Integer techId,
            HttpSession session) {

        // Admin access check
        Candidate loggedInUser = (Candidate) session.getAttribute("currentCandidate");
        if (loggedInUser == null || loggedInUser.getRole() != Candidate.Role.ADMIN) {
            return ResponseEntity.status(401).body("unauthorized");
        }

        try {
            boolean success = candidateService.removeTechnologyFromCandidate(candidateId, techId);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("not_found");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("error");
        }
    }
}