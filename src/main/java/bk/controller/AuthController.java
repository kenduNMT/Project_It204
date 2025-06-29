package bk.controller;

import bk.dto.CandidateLoginDTO;
import bk.dto.CandidateRegistrationDTO;
import bk.entity.Candidate;
import bk.exception.EmailAlreadyExistsException;
import bk.exception.InvalidCredentialsException;
import bk.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CandidateService candidateService;

    /**
     * Hiển thị form đăng nhập
     */
    @GetMapping("/login")
    public String showLoginForm(Model model, HttpSession session) {
        // Kiểm tra đã đăng nhập chưa
        if (session.getAttribute("currentCandidate") != null) {
            return "redirect:/candidate/dashboard";
        }

        model.addAttribute("loginDTO", new CandidateLoginDTO());
        return "auth/login";
    }

    /**
     * Xử lý đăng nhập
     */
    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginDTO") CandidateLoginDTO loginDTO,
                               BindingResult result,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (result.hasErrors()) {
            return "auth/login";
        }

        try {
            Candidate candidate = candidateService.login(loginDTO);

            // Kiểm tra trạng thái tài khoản
            if (candidate.getStatus() == Candidate.Status.INACTIVE) {
                model.addAttribute("errorMessage", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên để được hỗ trợ.");
                return "auth/login";
            }

            // Lưu thông tin vào session
            session.setAttribute("currentCandidate", candidate);
            session.setAttribute("candidateId", candidate.getId());
            session.setAttribute("candidateName", candidate.getName());
            session.setAttribute("role", candidate.getRole().toString());
            session.setAttribute("candidateEmail", candidate.getEmail());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng nhập thành công! Chào mừng " + candidate.getName());

            // Phân quyền: nếu là Admin thì redirect admin dashboard
            if (candidate.getRole() == Candidate.Role.ADMIN) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/candidate/dashboard";
            }

        } catch (InvalidCredentialsException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
        }
    }

    /**
     * Hiển thị form đăng ký
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpSession session) {
        // Kiểm tra đã đăng nhập chưa
        if (session.getAttribute("currentCandidate") != null) {
            return "redirect:/candidate/dashboard";
        }

        model.addAttribute("registrationDTO", new CandidateRegistrationDTO());
        return "auth/register";
    }

    /**
     * Xử lý đăng ký
     */
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registrationDTO") CandidateRegistrationDTO registrationDTO,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        // Kiểm tra mật khẩu xác nhận
        if (!registrationDTO.isPasswordMatching()) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
            return "auth/register";
        }

        try {
            Candidate candidate = candidateService.register(registrationDTO);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.");

            return "redirect:/auth/login";

        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    /**
     * Đăng xuất
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Đăng xuất thành công!");
        return "redirect:/";
    }

    /**
     * Kiểm tra email đã tồn tại (AJAX)
     */
    @PostMapping("/check-email")
    @ResponseBody
    public boolean checkEmailExists(@RequestParam String email) {
        return candidateService.findByEmail(email).isPresent();
    }

}