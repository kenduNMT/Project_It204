
package bk.controller;

import bk.entity.RecruitmentPosition;
import bk.entity.Technology;
import bk.service.RecruitmentPositionService;
import bk.utils.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/recruitment-position")
public class RecruitmentPositionController {

    @Autowired
    private RecruitmentPositionService recruitmentPositionService;

    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size,
                        @RequestParam(value = "search", required = false) String search,
                        @RequestParam(value = "filter", required = false) String filter) {

        System.out.println("=== DEBUG: Starting index method ===");
        System.out.println("Page: " + page + ", Size: " + size + ", Search: " + search + ", Filter: " + filter);

        PageResponse<RecruitmentPosition> pageResponse;
        String baseUrl = "/admin/recruitment-position";

        // Handle different filters and search
        if (search != null && !search.trim().isEmpty()) {
            pageResponse = recruitmentPositionService.findByName(search.trim(), page, size);
            baseUrl += "?search=" + search;
            model.addAttribute("search", search);
        } else if ("active".equals(filter)) {
            pageResponse = recruitmentPositionService.findActivePositions(page, size);
            baseUrl += "?filter=active";
            model.addAttribute("filter", filter);
        } else if ("expired".equals(filter)) {
            pageResponse = recruitmentPositionService.findExpiredPositions(page, size);
            baseUrl += "?filter=expired";
            model.addAttribute("filter", filter);
        } else {
            pageResponse = recruitmentPositionService.findAll(page, size);
            model.addAttribute("filter", "all");
        }

        System.out.println("Retrieved positions count: " + pageResponse.getContent().size());
        System.out.println("Total elements: " + pageResponse.getTotalElements());
        System.out.println("Total pages: " + pageResponse.getTotalPages());

        // Statistics
        Long totalCount = recruitmentPositionService.countAll();
        Long activeCount = recruitmentPositionService.countActive();

        System.out.println("Total count from service: " + totalCount);
        System.out.println("Active count from service: " + activeCount);

        // Add attributes to model
        model.addAttribute("positions", pageResponse.getContent());
        model.addAttribute("currentPage", pageResponse.getCurrentPage());
        model.addAttribute("totalPages", pageResponse.getTotalPages());
        model.addAttribute("totalElements", pageResponse.getTotalElements());
        model.addAttribute("hasNext", pageResponse.isHasNext());
        model.addAttribute("hasPrevious", pageResponse.isHasPrevious());
        model.addAttribute("baseUrl", baseUrl);

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("activeCount", activeCount);

        System.out.println("=== DEBUG: End index method ===");
        return "admin/recruitment-position/index";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("recruitmentPosition", new RecruitmentPosition());
        model.addAttribute("technologies", recruitmentPositionService.getAllActiveTechnologies());
        model.addAttribute("isEdit", false);
        return "admin/recruitment-position/form";
    }

    @PostMapping("/add")
    public String processAdd(@Valid @ModelAttribute RecruitmentPosition recruitmentPosition,
                             BindingResult result,
                             @RequestParam(value = "technologyIds", required = false) List<Integer> technologyIds,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        // Check if name already exists
        if (recruitmentPositionService.isNameExists(recruitmentPosition.getName(), null)) {
            result.rejectValue("name", "error.name", "Tên vị trí đã tồn tại");
        }

        if (result.hasErrors()) {
            model.addAttribute("technologies", recruitmentPositionService.getAllActiveTechnologies());
            model.addAttribute("selectedTechnologyIds", technologyIds);
            model.addAttribute("isEdit", false);
            return "admin/recruitment-position/form";
        }

        try {
            recruitmentPositionService.save(recruitmentPosition, technologyIds);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm vị trí tuyển dụng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi thêm vị trí tuyển dụng!");
        }

        return "redirect:/admin/recruitment-position";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        RecruitmentPosition recruitmentPosition = recruitmentPositionService.findById(id);

        if (recruitmentPosition == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy vị trí tuyển dụng!");
            return "redirect:/admin/recruitment-position";
        }

        List<Integer> selectedTechnologyIds = recruitmentPosition.getTechnologies()
                .stream()
                .map(Technology::getId)
                .collect(Collectors.toList());

        model.addAttribute("recruitmentPosition", recruitmentPosition);
        model.addAttribute("technologies", recruitmentPositionService.getAllActiveTechnologies());
        model.addAttribute("selectedTechnologyIds", selectedTechnologyIds);
        model.addAttribute("isEdit", true);

        return "admin/recruitment-position/form";
    }

    @PostMapping("/edit/{id}")
    public String processEdit(@PathVariable Integer id,
                              @Valid @ModelAttribute RecruitmentPosition recruitmentPosition,
                              BindingResult result,
                              @RequestParam(value = "technologyIds", required = false) List<Integer> technologyIds,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        recruitmentPosition.setId(id);

        // Check if name already exists (exclude current record)
        if (recruitmentPositionService.isNameExists(recruitmentPosition.getName(), id)) {
            result.rejectValue("name", "error.name", "Tên vị trí đã tồn tại");
        }

        if (result.hasErrors()) {
            model.addAttribute("technologies", recruitmentPositionService.getAllActiveTechnologies());
            model.addAttribute("selectedTechnologyIds", technologyIds);
            model.addAttribute("isEdit", true);
            return "admin/recruitment-position/form";
        }

        try {
            recruitmentPositionService.update(recruitmentPosition, technologyIds);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật vị trí tuyển dụng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật vị trí tuyển dụng!");
        }

        return "redirect:/admin/recruitment-position";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            RecruitmentPosition recruitmentPosition = recruitmentPositionService.findById(id);
            if (recruitmentPosition == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy vị trí tuyển dụng!");
            } else {
                recruitmentPositionService.delete(id);
                redirectAttributes.addFlashAttribute("successMessage", "Xóa vị trí tuyển dụng thành công!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa vị trí tuyển dụng!");
        }

        return "redirect:/admin/recruitment-position";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        RecruitmentPosition recruitmentPosition = recruitmentPositionService.findById(id);

        if (recruitmentPosition == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy vị trí tuyển dụng!");
            return "redirect:/admin/recruitment-position";
        }

        model.addAttribute("recruitmentPosition", recruitmentPosition);
        return "admin/recruitment-position/view";
    }

    @GetMapping("/active")
    public String activePositions(Model model,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size) {
        return index(model, page, size, null, "active");
    }

    @GetMapping("/expired")
    public String expiredPositions(Model model,
                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "10") int size) {
        return index(model, page, size, null, "expired");
    }
}