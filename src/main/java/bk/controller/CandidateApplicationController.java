package bk.controller;

import bk.entity.Application;
import bk.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/candidate")
public class CandidateApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/applications")
    public String viewApplications(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "status", required = false) String status,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check login
        Long candidateIdLong = (Long) session.getAttribute("candidateId");
        if (candidateIdLong == null) {
            System.out.println("No candidate in session");
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xem đơn ứng tuyển");
            return "redirect:/auth/login";
        }

        int candidateId = candidateIdLong.intValue();
        System.out.println("CandidateId: " + candidateId);

        try {
            // Get applications for the current candidate
            List<Application> applications;
            long totalApplications;

            if (status != null && !status.isEmpty() && !status.equals("all")) {
                // Filter by status
                Application.Status applicationStatus = Application.Status.valueOf(status.toUpperCase());
                applications = applicationService.findByCandidateIdAndStatus(candidateId, applicationStatus, page, size);
                totalApplications = applicationService.countByCandidateIdAndStatus(candidateId, applicationStatus);
            } else {
                // Get all applications for this candidate
                applications = applicationService.findByCandidateId(candidateId, page, size);
                totalApplications = applicationService.countByCandidateId(candidateId);
            }

            // Calculate statistics
            long totalCount = applicationService.countByCandidateId(candidateId);
            long pendingCount = applicationService.countByCandidateIdAndStatus(candidateId, Application.Status.PENDING);
            long approvedCount = applicationService.countByCandidateIdAndStatus(candidateId, Application.Status.APPROVED);
            long rejectedCount = applicationService.countByCandidateIdAndStatus(candidateId, Application.Status.REJECTED);
            long interviewingCount = applicationService.countByCandidateIdAndStatus(candidateId, Application.Status.INTERVIEWING);

            // Calculate pagination
            int totalPages = (int) Math.ceil((double) totalApplications / size);
            boolean hasNext = page < totalPages;
            boolean hasPrevious = page > 1;

            // Add data to model
            model.addAttribute("applications", applications);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("hasNext", hasNext);
            model.addAttribute("hasPrevious", hasPrevious);
            model.addAttribute("selectedStatus", status);

            // Statistics
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("interviewingCount", interviewingCount);

            // Candidate info from session
            model.addAttribute("candidateName", session.getAttribute("candidateName"));
            model.addAttribute("candidateEmail", session.getAttribute("candidateEmail"));

            System.out.println("Found " + applications.size() + " applications for candidate " + candidateId);
            return "candidate/applications";

        } catch (Exception e) {
            System.err.println("Error loading applications for candidate " + candidateId + ": " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tải danh sách đơn ứng tuyển");
            return "redirect:/candidate/dashboard";
        }
    }

    @GetMapping("/applications/detail")
    public String viewApplicationDetail(
            @RequestParam("id") Long applicationId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check login
        Long candidateIdLong = (Long) session.getAttribute("candidateId");
        if (candidateIdLong == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/auth/login";
        }

        try {
            Application application = applicationService.findById(applicationId);

            if (application == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn ứng tuyển");
                return "redirect:/candidate/applications";
            }

            // Check if application belongs to current candidate
            if (!application.getCandidate().getId().equals(candidateIdLong.intValue())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xem đơn ứng tuyển này");
                return "redirect:/candidate/applications";
            }

            model.addAttribute("application", application);
            return "candidate/application-detail";

        } catch (Exception e) {
            System.err.println("Error loading application detail: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tải chi tiết đơn ứng tuyển");
            return "redirect:/candidate/applications";
        }
    }

    @GetMapping("/applications/withdraw")
    public String withdrawApplication(
            @RequestParam("id") Long applicationId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Check login
        Long candidateIdLong = (Long) session.getAttribute("candidateId");
        if (candidateIdLong == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/auth/login";
        }

        try {
            Application application = applicationService.findById(applicationId);

            if (application == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn ứng tuyển");
                return "redirect:/candidate/applications";
            }

            // Check if application belongs to current candidate
            if (!application.getCandidate().getId().equals(candidateIdLong.intValue())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thao tác với đơn ứng tuyển này");
                return "redirect:/candidate/applications";
            }

            // Check if application can be withdrawn
            if (application.getStatus() == Application.Status.APPROVED ||
                    application.getStatus() == Application.Status.REJECTED) {
                redirectAttributes.addFlashAttribute("error", "Không thể rút đơn ứng tuyển đã được duyệt hoặc từ chối");
                return "redirect:/candidate/applications";
            }

            // Update status to withdrawn (you might need to add WITHDRAWN status to your enum)
            application.setStatus(Application.Status.REJECTED); // or create WITHDRAWN status
            applicationService.update(application);

            redirectAttributes.addFlashAttribute("success", "Đã rút đơn ứng tuyển thành công");
            return "redirect:/candidate/applications";

        } catch (Exception e) {
            System.err.println("Error withdrawing application: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi rút đơn ứng tuyển");
            return "redirect:/candidate/applications";
        }
    }
}