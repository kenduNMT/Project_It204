package bk.controller;

import bk.dto.CandidateProfileUpdateDTO;
import bk.entity.Application;
import bk.entity.Candidate;
import bk.entity.Technology;
import bk.service.ApplicationService;
import bk.service.CandidateService;
import bk.service.TechnologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/candidate")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private ApplicationService applicationService;
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

        long totalCount = applicationService.countByCandidateId(Math.toIntExact(candidate.getId()));
        long pendingCount = applicationService.countByCandidateIdAndStatus(Math.toIntExact(candidate.getId()), Application.Status.PENDING);
        long approvedCount = applicationService.countByCandidateIdAndStatus(Math.toIntExact(candidate.getId()), Application.Status.APPROVED);

        // Thống kê cơ bản
        model.addAttribute("totalApplications", totalCount);
        model.addAttribute("pendingApplications", pendingCount);
        model.addAttribute("acceptedApplications", approvedCount);

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

            // Lấy danh sách công nghệ của ứng viên
            List<Technology> candidateTechnologies = candidateService.getCandidateTechnologies(updatedCandidate.get().getId());
            model.addAttribute("candidateTechnologies", candidateTechnologies);
        } else {
            model.addAttribute("candidate", candidate);
            // Lấy danh sách công nghệ của ứng viên
            List<Technology> candidateTechnologies = candidateService.getCandidateTechnologies(candidate.getId());
            model.addAttribute("candidateTechnologies", candidateTechnologies);
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

        // Nếu model chưa có attribute "candidateDTO", ta tạo mới
        if (!model.containsAttribute("candidateDTO")) {
            Candidate candidate = getCurrentCandidate(session);
            // Lấy thông tin mới nhất từ database để đảm bảo dữ liệu luôn đúng
            Optional<Candidate> updatedCandidateOpt = candidateService.findById(candidate.getId());
            Candidate updatedCandidate = updatedCandidateOpt.orElse(candidate);

            // Chuyển từ Entity sang DTO để hiển thị trên form
            CandidateProfileUpdateDTO dto = new CandidateProfileUpdateDTO();
            dto.setName(updatedCandidate.getName());
            dto.setPhone(updatedCandidate.getPhone());
            dto.setGender(updatedCandidate.getGender());
            dto.setDob(updatedCandidate.getDob());
            dto.setDescription(updatedCandidate.getDescription());
            dto.setExperience(updatedCandidate.getExperience());

            model.addAttribute("candidateDTO", dto);
            model.addAttribute("candidate", updatedCandidate); // Vẫn giữ để dùng cho sidebar, header...
        } else {
            // Nếu có lỗi validation, ta cần lấy lại thông tin candidate đầy đủ
            model.addAttribute("candidate", getCurrentCandidate(session));
        }

        // Lấy danh sách công nghệ
        Candidate currentCandidate = getCurrentCandidate(session);
        List<Technology> candidateTechnologies = candidateService.getCandidateTechnologies(currentCandidate.getId());
        List<Technology> allTechnologies = technologyService.getAllActiveTechnologies();
        model.addAttribute("candidateTechnologies", candidateTechnologies);
        model.addAttribute("allTechnologies", allTechnologies);

        return "candidate/edit-profile";
    }

    /**
     * Xử lý cập nhật profile
     */
    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("candidateDTO") CandidateProfileUpdateDTO dto,
                                BindingResult result,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            // Nếu có lỗi, thêm DTO và lỗi vào redirect a`ttributes để hiển thị lại form
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.candidateDTO", result);
            redirectAttributes.addFlashAttribute("candidateDTO", dto);
            // Redirect về trang edit để tránh lỗi F5 (resubmission)
            return "redirect:/candidate/profile/edit";
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
            // Khi có lỗi, cần load lại danh sách công nghệ
            Candidate candidate = getCurrentCandidate(session);
            List<Technology> candidateTechnologies = candidateService.getCandidateTechnologies(candidate.getId());
            List<Technology> allTechnologies = technologyService.getAllActiveTechnologies();

            model.addAttribute("candidateTechnologies", candidateTechnologies);
            model.addAttribute("allTechnologies", allTechnologies);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật thông tin: " + e.getMessage());

            return "candidate/edit-profile";
        }
    }

    /**
     * Hiển thị trang quản lý công nghệ
     */
    @GetMapping("/technologies")
    public String manageTechnologies(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        Candidate candidate = getCurrentCandidate(session);
        model.addAttribute("candidate", candidate);

        // Lấy danh sách công nghệ của ứng viên
        List<Technology> candidateTechnologies = candidateService.getCandidateTechnologies(candidate.getId());
        model.addAttribute("candidateTechnologies", candidateTechnologies);

        // Lấy danh sách tất cả công nghệ để thêm mới
        List<Technology> allTechnologies = technologyService.getAllActiveTechnologies();
        model.addAttribute("allTechnologies", allTechnologies);

        return "candidate/technologies";
    }

    /**
     * Thêm công nghệ cho ứng viên (từ trang edit profile)
     */
    @PostMapping("/profile/technologies/add")
    public String addTechnologyFromEdit(@RequestParam("technologyId") Integer technologyId,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        try {
            Candidate candidate = getCurrentCandidate(session);
            candidateService.addTechnology(candidate.getId(), technologyId);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm công nghệ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/candidate/profile/edit";
    }

    /**
     * Xóa công nghệ khỏi ứng viên (từ trang edit profile)
     */
    @PostMapping("/profile/technologies/remove")
    public String removeTechnologyFromEdit(@RequestParam("technologyId") Integer technologyId,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        try {
            Candidate candidate = getCurrentCandidate(session);
            candidateService.removeTechnology(candidate.getId(), technologyId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa công nghệ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/candidate/profile/edit";
    }

    /**
     * Thêm công nghệ cho ứng viên
     */
    @PostMapping("/technologies/add")
    public String addTechnology(@RequestParam("technologyId") Integer technologyId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        try {
            Candidate candidate = getCurrentCandidate(session);
            candidateService.addTechnology(candidate.getId(), technologyId);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm công nghệ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/candidate/technologies";
    }

    /**
     * Xóa công nghệ khỏi ứng viên
     */
    @PostMapping("/technologies/remove")
    public String removeTechnology(@RequestParam("technologyId") Integer technologyId,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        try {
            Candidate candidate = getCurrentCandidate(session);
            candidateService.removeTechnology(candidate.getId(), technologyId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa công nghệ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/candidate/technologies";
    }

    /**
     * Đổi mật khẩu - hiển thị form
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        Candidate candidate = getCurrentCandidate(session);
        model.addAttribute("candidate", candidate);

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

        // Kiểm tra mật khẩu mới và xác nhận
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "candidate/change-password";
        }

        // Kiểm tra độ dài mật khẩu
        if (newPassword.length() < 6) {
            model.addAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "candidate/change-password";
        }

        try {
            Candidate candidate = getCurrentCandidate(session);

            // Kiểm tra mật khẩu hiện tại
            if (!candidateService.checkPassword(candidate.getId(), currentPassword)) {
                model.addAttribute("errorMessage", "Mật khẩu hiện tại không đúng!");
                return "candidate/change-password";
            }

            // Cập nhật mật khẩu mới
            candidateService.updatePassword(candidate.getId(), newPassword);

            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            return "redirect:/candidate/profile";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi đổi mật khẩu: " + e.getMessage());
            return "candidate/change-password";
        }
    }
}