package bk.controller;

import bk.dto.CandidateProfileUpdateDTO;
import bk.entity.Candidate;
import bk.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/candidate")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    /**
     * Kiểm tra đăng nhập
     */
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("currentCandidate") != null;
    }

    /**
     * Lấy ứng viên hiện tại từ session
     */
    private Candidate getCurrentCandidate(HttpSession session) {
        return (Candidate) session.getAttribute("currentCandidate");
    }

    /**
     * Dashboard ứng viên
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        Candidate candidate = getCurrentCandidate(session);
        model.addAttribute("candidate", candidate);

        // Thống kê cơ bản
        model.addAttribute("totalApplications", 0); // Sẽ cập nhật sau khi có bảng applications
        model.addAttribute("pendingApplications", 0);
        model.addAttribute("acceptedApplications", 0);

        return "candidate/dashboard";
    }

    /**
     * Hiển thị thông tin profile
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        Candidate candidate = getCurrentCandidate(session);
        // Lấy thông tin mới nhất từ database
        Optional<Candidate> updatedCandidate = candidateService.findById(candidate.getId());

        if (updatedCandidate.isPresent()) {
            model.addAttribute("candidate", updatedCandidate.get());
            // Cập nhật session
            session.setAttribute("currentCandidate", updatedCandidate.get());
        } else {
            model.addAttribute("candidate", candidate);
        }

        return "candidate/profile";
    }

    /**
     * Hiển thị form chỉnh sửa profile
     */
    @GetMapping("/profile/edit")
    public String showEditProfile(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        Candidate candidate = getCurrentCandidate(session);
        // Lấy thông tin mới nhất từ database
        Optional<Candidate> updatedCandidate = candidateService.findById(candidate.getId());

        if (updatedCandidate.isPresent()) {
            model.addAttribute("candidate", updatedCandidate.get());
        } else {
            model.addAttribute("candidate", candidate);
        }

        return "candidate/edit-profile";
    }

    /**
     * Xử lý cập nhật profile
     */
    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("candidate") CandidateProfileUpdateDTO dto,
                                BindingResult result,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            return "candidate/edit-profile";
        }

        try {
            Candidate currentCandidate = getCurrentCandidate(session);

            // Cập nhật các thông tin được phép thay đổi
            currentCandidate.setName(dto.getName());
            currentCandidate.setPhone(dto.getPhone());
            currentCandidate.setGender(dto.getGender());
            currentCandidate.setDob(dto.getDob());
            currentCandidate.setDescription(dto.getDescription());
            currentCandidate.setExperience(dto.getExperience());

            // Lưu vào database
            Candidate updatedCandidate = candidateService.updateProfile(currentCandidate);

            // Cập nhật session
            session.setAttribute("currentCandidate", updatedCandidate);
            session.setAttribute("candidateName", updatedCandidate.getName());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật thông tin thành công!");

            return "redirect:/candidate/profile";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật thông tin: " + e.getMessage());
            return "candidate/edit-profile";
        }
    }

    /**
     * Hiển thị danh sách đơn ứng tuyển
     */
    @GetMapping("/applications")
    public String showApplications(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        Candidate candidate = getCurrentCandidate(session);
        model.addAttribute("candidate", candidate);

        // TODO: Lấy danh sách đơn ứng tuyển khi có bảng applications
        model.addAttribute("applications", java.util.Collections.emptyList());

        return "candidate/applications";
    }

    /**
     * Hiển thị danh sách việc làm
     */
    @GetMapping("/jobs")
    public String showJobs(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        Candidate candidate = getCurrentCandidate(session);
        model.addAttribute("candidate", candidate);

        // TODO: Lấy danh sách việc làm khi có bảng recruitment_position
        model.addAttribute("jobs", java.util.Collections.emptyList());

        return "candidate/jobs";
    }

    /**
     * Đổi mật khẩu - hiển thị form
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        return "candidate/change-password";
    }

    /**
     * Xử lý đổi mật khẩu
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        // Validation
        if (newPassword == null || newPassword.length() < 6) {
            model.addAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự");
            return "candidate/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
            return "candidate/change-password";
        }

        try {
            Candidate candidate = getCurrentCandidate(session);

            // Kiểm tra mật khẩu hiện tại
            if (!candidateService.checkPassword(currentPassword, candidate.getPassword())) {
                model.addAttribute("errorMessage", "Mật khẩu hiện tại không chính xác");
                return "candidate/change-password";
            }

            // Cập nhật mật khẩu mới
            candidate.setPassword(candidateService.encodePassword(newPassword));
            candidateService.updateProfile(candidate);

            // Cập nhật session
            session.setAttribute("currentCandidate", candidate);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đổi mật khẩu thành công!");

            return "redirect:/candidate/profile";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "candidate/change-password";
        }
    }
}