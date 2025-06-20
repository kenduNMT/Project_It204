package bk.controller;

import bk.entity.RecruitmentPosition;
import bk.entity.Technology;
import bk.service.RecruitmentPositionService;
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
    public String index(Model model, @RequestParam(value = "search", required = false) String search) {
        System.out.println("=== DEBUG: Starting index method ===");

        List<RecruitmentPosition> positions;
        if (search != null && !search.trim().isEmpty()) {
            System.out.println("Searching with keyword: " + search);
            positions = recruitmentPositionService.findByName(search.trim());
            model.addAttribute("search", search);
        } else {
            System.out.println("Getting all positions");
            positions = recruitmentPositionService.findAll();
        }

        System.out.println("Retrieved positions count: " + (positions != null ? positions.size() : 0));

        if (positions != null && !positions.isEmpty()) {
            System.out.println("First position details:");
            RecruitmentPosition first = positions.get(0);
            System.out.println("- ID: " + first.getId());
            System.out.println("- Name: " + first.getName());
            System.out.println("- IsDeleted: " + first.getIsDeleted());
            System.out.println("- CreatedDate: " + first.getCreatedDate());
            System.out.println("- ExpiredDate: " + first.getExpiredDate());
            System.out.println("- IsActive: " + first.isActive());
            System.out.println("- Technologies count: " +
                    (first.getTechnologies() != null ? first.getTechnologies().size() : 0));
        }

        Long totalCount = recruitmentPositionService.countAll();
        Long activeCount = recruitmentPositionService.countActive();

        System.out.println("Total count from service: " + totalCount);
        System.out.println("Active count from service: " + activeCount);

        model.addAttribute("positions", positions);
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
    public String activePositions(Model model) {
        List<RecruitmentPosition> positions = recruitmentPositionService.findActivePositions();
        model.addAttribute("positions", positions);
        model.addAttribute("pageTitle", "Vị trí đang tuyển dụng");
        return "admin/recruitment-position/index";
    }

    @GetMapping("/expired")
    public String expiredPositions(Model model) {
        List<RecruitmentPosition> positions = recruitmentPositionService.findExpiredPositions();
        model.addAttribute("positions", positions);
        model.addAttribute("pageTitle", "Vị trí đã hết hạn");
        return "admin/recruitment-position/index";
    }
}