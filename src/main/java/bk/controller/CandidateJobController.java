package bk.controller;

import bk.entity.Application;
import bk.entity.Candidate;
import bk.entity.RecruitmentPosition;
import bk.service.ApplicationService;
import bk.service.CandidateService;
import bk.service.RecruitmentPositionService;
import bk.utils.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/candidate")
public class CandidateJobController {

    @Autowired
    private RecruitmentPositionService recruitmentPositionService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CandidateService candidateService;

    /**
     * Hiển thị trang tìm việc làm cho ứng viên với tìm kiếm và lọc
     */
    @GetMapping("/jobs")
    public String showJobsPage(Model model,
                               @RequestParam(value = "page", defaultValue = "1") int page,
                               @RequestParam(value = "size", defaultValue = "5") int size,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "location", required = false) String location,
                               @RequestParam(value = "category", required = false) String category,
                               @RequestParam(value = "minSalary", required = false) Double minSalary,
                               @RequestParam(value = "maxSalary", required = false) Double maxSalary,
                               @RequestParam(value = "minExperience", required = false) Integer minExperience,
                               @RequestParam(value = "sortBy", defaultValue = "newest") String sortBy,
                               HttpSession session) {

        // Kiểm tra đăng nhập
        if (session.getAttribute("candidateId") == null) {
            return "redirect:/auth/login";
        }

        // Convert page to 0-based for service layer
        int pageIndex = page - 1;
        if (pageIndex < 0) pageIndex = 0;

        // Áp dụng tìm kiếm và lọc
        PageResponse<RecruitmentPosition> jobsPage = searchAndFilterJobs(
                keyword, location, category, minSalary, maxSalary, minExperience, sortBy, pageIndex, size);

        // DEBUG: In ra thông tin để kiểm tra
        System.out.println("Search params - keyword: " + keyword + ", location: " + location +
                ", minSalary: " + minSalary + ", maxSalary: " + maxSalary);
        System.out.println("Number of jobs found: " + jobsPage.getContent().size());

        // Ensure technologies are loaded for all jobs
        jobsPage.getContent().forEach(job -> {
            if (job.getTechnologies() != null) {
                job.getTechnologies().size(); // Force initialization
            }
        });

        // Add data to model
        model.addAttribute("jobs", jobsPage.getContent());
        model.addAttribute("currentPage", jobsPage.getCurrentPage() + 1); // Convert to 1-based
        model.addAttribute("totalPages", jobsPage.getTotalPages());
        model.addAttribute("totalElements", jobsPage.getTotalElements());
        model.addAttribute("hasNext", jobsPage.isHasNext());
        model.addAttribute("hasPrevious", jobsPage.isHasPrevious());

        // Add search parameters to model for form retention
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        model.addAttribute("category", category);
        model.addAttribute("minSalary", minSalary);
        model.addAttribute("maxSalary", maxSalary);
        model.addAttribute("minExperience", minExperience);
        model.addAttribute("sortBy", sortBy);

        // Statistics for header
        long totalActiveJobs = recruitmentPositionService.countActive();
        model.addAttribute("totalJobs", totalActiveJobs);

        // Add pagination display info
        int startItem = (jobsPage.getCurrentPage() * size) + 1;
        int endItem = Math.min(startItem + jobsPage.getContent().size() - 1,
                (int) jobsPage.getTotalElements());
        model.addAttribute("startItem", startItem);
        model.addAttribute("endItem", endItem);

        return "candidate/jobs";
    }

    /**
     * Xem chi tiết việc làm
     */
    @GetMapping("/jobs/{id}/detail")
    public String showJobDetail(@PathVariable Integer id, Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        if (session.getAttribute("candidateId") == null) {
            return "redirect:/auth/login";
        }

        try {
            RecruitmentPosition job = recruitmentPositionService.findById(id);
            if (job == null || !job.isActive()) {
                model.addAttribute("error", "Việc làm không tồn tại hoặc đã hết hạn");
                return "redirect:/candidate/jobs";
            }

            // Ensure technologies are loaded
            if (job.getTechnologies() != null) {
                job.getTechnologies().size(); // Force initialize
            }

            // Redirect to apply page instead of showing detail page
            return "redirect:/candidate/jobs/" + id + "/apply";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải thông tin việc làm");
            return "redirect:/candidate/jobs";
        }
    }

    /**
     * Hiển thị trang ứng tuyển (redirect đến cv-upload.html)
     */
    @GetMapping("/jobs/{id}/apply")
    public String showApplyPage(@PathVariable Integer id, Model model, HttpSession session) {
        try {
            // Kiểm tra đăng nhập
            Long candidateIdLong = (Long) session.getAttribute("candidateId");
            if (candidateIdLong == null) {
                return "redirect:/auth/login";
            }

            // An toàn hơn khi convert Long to int
            int candidateId;
            try {
                candidateId = Math.toIntExact(candidateIdLong);
            } catch (ArithmeticException e) {
                System.err.println("Error converting candidateId from Long to int: " + e.getMessage());
                model.addAttribute("error", "Lỗi xác thực người dùng");
                return "redirect:/candidate/jobs";
            }

            // Kiểm tra việc làm có tồn tại không
            RecruitmentPosition job = null;
            try {
                job = recruitmentPositionService.findById(id);
            } catch (Exception e) {
                System.err.println("Error finding job by id " + id + ": " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("error", "Không thể tải thông tin việc làm");
                return "redirect:/candidate/jobs";
            }

            if (job == null) {
                model.addAttribute("error", "Việc làm không tồn tại");
                return "redirect:/candidate/jobs";
            }

            if (!job.isActive()) {
                model.addAttribute("error", "Việc làm đã hết hạn");
                return "redirect:/candidate/jobs";
            }

            // Kiểm tra đã ứng tuyển chưa
            boolean hasApplied = false;
            try {
                hasApplied = applicationService.hasApplied(candidateId, id);
            } catch (Exception e) {
                System.err.println("Error checking if applied: " + e.getMessage());
                e.printStackTrace();
                // Không return lỗi ở đây, vì có thể chưa ứng tuyển
            }

            if (hasApplied) {
                model.addAttribute("error", "Bạn đã ứng tuyển vị trí này rồi");
                return "redirect:/candidate/jobs";
            }

            // Lấy thông tin ứng viên
            Optional<Candidate> candidateOpt = null;
            try {
                candidateOpt = candidateService.findById(candidateIdLong);
            } catch (Exception e) {
                System.err.println("Error finding candidate by id " + candidateIdLong + ": " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("error", "Không thể tải thông tin ứng viên");
                return "redirect:/candidate/jobs";
            }

            if (candidateOpt == null || !candidateOpt.isPresent()) {
                model.addAttribute("error", "Không tìm thấy thông tin ứng viên");
                return "redirect:/candidate/jobs";
            }

            // Force load technologies để tránh lazy loading exception
            try {
                if (job.getTechnologies() != null) {
                    job.getTechnologies().size(); // Force initialization
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not load job technologies: " + e.getMessage());
                // Không cần return lỗi vì technologies không phải là required
            }

            // Add debug info
            System.out.println("Successfully loaded apply page for job ID: " + id + ", candidate ID: " + candidateId);

            model.addAttribute("job", job);
            model.addAttribute("candidate", candidateOpt.get());
            model.addAttribute("jobId", id);

            return "candidate/cv-upload"; // Redirect đến trang cv-upload.html

        } catch (Exception e) {
            System.err.println("Unexpected error in showApplyPage: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải trang ứng tuyển");
            return "redirect:/candidate/jobs";
        }
    }

    /**
     * Xử lý ứng tuyển việc làm (POST từ cv-upload.html)
     */
    @PostMapping("/jobs/{jobId}/apply")
    public String applyJob(@PathVariable Integer jobId,
                           @RequestParam(required = false) String coverLetter,
                           @RequestParam(required = false) String cvUrl,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== DEBUG Apply Job ===");
            System.out.println("JobId: " + jobId);
            System.out.println("CvUrl: " + cvUrl);
            System.out.println("CoverLetter: " + coverLetter);

            // Check login
            Long candidateIdLong = (Long) session.getAttribute("candidateId");
            if (candidateIdLong == null) {
                System.out.println("No candidate in session");
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để ứng tuyển");
                return "redirect:/auth/login";
            }

            int candidateId = candidateIdLong.intValue();
            System.out.println("CandidateId: " + candidateId);

            // Validate CV URL first
            if (cvUrl == null || cvUrl.trim().isEmpty()) {
                System.out.println("CV URL is empty");
                redirectAttributes.addFlashAttribute("error", "Vui lòng cung cấp CV để ứng tuyển");
                return "redirect:/candidate/jobs/" + jobId + "/apply";
            }

            // Check if job exists and is active
            RecruitmentPosition job = recruitmentPositionService.findById(jobId);
            if (job == null) {
                System.out.println("Job not found");
                redirectAttributes.addFlashAttribute("error", "Việc làm không tồn tại");
                return "redirect:/candidate/jobs";
            }

            if (!job.isActive()) {
                System.out.println("Job is not active");
                redirectAttributes.addFlashAttribute("error", "Việc làm đã hết hạn");
                return "redirect:/candidate/jobs";
            }

            // Check if already applied
            boolean hasApplied = applicationService.hasApplied(candidateId, jobId);
            if (hasApplied) {
                System.out.println("Already applied");
                redirectAttributes.addFlashAttribute("error", "Bạn đã ứng tuyển vị trí này rồi");
                return "redirect:/candidate/jobs";
            }

            // Get candidate entity from database
            Optional<Candidate> candidateOpt = candidateService.findById(candidateIdLong);
            if (!candidateOpt.isPresent()) {
                System.out.println("Candidate not found in database");
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin ứng viên");
                return "redirect:/candidate/jobs";
            }

            // Create application with proper entity relationships
            Application application = new Application();

            // Set candidate - use the actual entity from database
            application.setCandidate(candidateOpt.get());

            // Set recruitment position - use the actual entity from database
            application.setRecruitmentPosition(job);

            // Set application details
            application.setStatus(Application.Status.PENDING);
            application.setCreatedAt(LocalDate.now().atStartOfDay());

            // Based on your database schema, it should be:
            application.setCvUrl(cvUrl);

            // Set cover letter - check your Application entity field name
            // If coverLetter field exists, use it; otherwise use interviewResultNote temporarily
            if (coverLetter != null && !coverLetter.trim().isEmpty()) {
                // application.setCoverLetter(coverLetter); // If this field exists
                application.setInterviewResultNote(coverLetter); // Using this field temporarily
            }

            // Set default values for required fields
            application.setInterviewResult("PENDING"); // Don't use "NULL" string

            System.out.println("Saving application...");
            System.out.println("Application candidate ID: " + application.getCandidate().getId());
            System.out.println("Application job ID: " + application.getRecruitmentPosition().getId());
            System.out.println("Application CV URL: " + application.getCvUrl());

            // Save application
            Application savedApplication = applicationService.save(application);
            System.out.println("Application saved with ID: " + savedApplication.getId());

            redirectAttributes.addFlashAttribute("success",
                    "Ứng tuyển thành công! Chúng tôi sẽ liên hệ với bạn sớm nhất có thể.");

            return "redirect:/candidate/applications";

        } catch (Exception e) {
            System.err.println("Error in applyJob: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Có lỗi xảy ra khi ứng tuyển: " + e.getMessage());
            return "redirect:/candidate/jobs/" + jobId + "/apply";
        }
    }

    /**
     * Search and filter jobs - Private method
     */
    private PageResponse<RecruitmentPosition> searchAndFilterJobs(
            String keyword, String location, String category,
            Double minSalary, Double maxSalary, Integer minExperience,
            String sortBy, int page, int size) {

        try {
            return recruitmentPositionService.searchAndFilter(
                    keyword, location, category, minSalary, maxSalary,
                    minExperience, sortBy, page, size);
        } catch (Exception e) {
            e.printStackTrace();
            // Return empty page if error occurs
            return new PageResponse<>();
        }
    }

    /**
     * API endpoint for CV upload
     */
    @PostMapping("/api/upload/cv")
    @ResponseBody
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "File không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file type
            String contentType = file.getContentType();
            if (!"application/pdf".equals(contentType)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Chỉ chấp nhận file PDF");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file size (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "File quá lớn. Tối đa 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Save file
            String fileName = saveUploadedFile(file);
            String fileUrl = "/uploads/cv/" + fileName;

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "Upload thành công");
            successResponse.put("fileUrl", fileUrl);
            successResponse.put("fileName", fileName);

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Save uploaded file to server
     */
    private String saveUploadedFile(MultipartFile file) throws IOException {
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;

        // Create upload directory if not exists
        Path uploadPath = Paths.get("uploads/cv");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    /**
     * Get all available locations for filter
     */
    @GetMapping("/api/locations")
    @ResponseBody
    public ResponseEntity<List<String>> getAvailableLocations() {
        try {
            List<String> locations = recruitmentPositionService.getAvailableLocations();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * Get all available categories for filter
     */
    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<List<String>> getAvailableCategories() {
        try {
            List<String> categories = recruitmentPositionService.getAvailableCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * Get salary range statistics
     */
    @GetMapping("/api/salary-range")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSalaryRange() {
        try {
            Map<String, Object> salaryRange = recruitmentPositionService.getSalaryRange();
            return ResponseEntity.ok(salaryRange);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new HashMap<>());
        }
    }

    /**
     * Check if candidate has applied for a job
     */
    @GetMapping("/api/jobs/{jobId}/applied")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkApplied(
            @PathVariable Integer jobId, HttpSession session) {
        try {
            Long candidateIdLong = (Long) session.getAttribute("candidateId");
            if (candidateIdLong == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("applied", false);
                return ResponseEntity.ok(response);
            }
            int candidateId = candidateIdLong.intValue();
            boolean hasApplied = applicationService.hasApplied(candidateId, jobId);
            Map<String, Object> response = new HashMap<>();
            response.put("applied", hasApplied);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("applied", false);
            return ResponseEntity.status(500).body(errorResponse);
        }

    }
}