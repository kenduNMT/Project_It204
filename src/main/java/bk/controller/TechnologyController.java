package bk.controller;

import bk.entity.Technology;
import bk.service.TechnologyService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/technology")
public class TechnologyController {

    @Autowired
    private TechnologyService technologyService;

    // Hiển thị danh sách công nghệ
    @GetMapping("")
    public String listTechnologies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            Model model) {

        Page<Technology> technologies;

        if (search != null && !search.trim().isEmpty()) {
            technologies = technologyService.searchTechnologies(search.trim(), page, size);
            model.addAttribute("search", search);
        } else {
            technologies = technologyService.getAllActiveTechnologies(page, size);
        }

        model.addAttribute("technologies", technologies);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", technologies.getTotalPages());
        model.addAttribute("totalElements", technologies.getTotalElements());

        return "admin/technology/technology-list";
    }

    // Hiển thị form thêm công nghệ
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("technology", new Technology());
        model.addAttribute("action", "add");
        return "admin/technology/technology-form";
    }

    // Xử lý thêm công nghệ
    @PostMapping("/add")
    public String addTechnology(
            @Valid @ModelAttribute Technology technology,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("action", "add");
            return "admin/technology/technology-form";
        }

        try {
            technologyService.createTechnology(technology);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm công nghệ thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/technology";
    }

    // Hiển thị form sửa công nghệ
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Technology> technology = technologyService.findById(id);

        if (!technology.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy công nghệ!");
            return "redirect:/admin/technology";
        }

        model.addAttribute("technology", technology.get());
        model.addAttribute("action", "edit");
        return "admin/technology/technology-form";
    }

    // Xử lý cập nhật công nghệ
    @PostMapping("/edit/{id}")
    public String updateTechnology(
            @PathVariable Integer id,
            @Valid @ModelAttribute Technology technology,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("action", "edit");
            return "admin/technology/technology-form";
        }

        try {
            technologyService.updateTechnology(id, technology);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật công nghệ thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/technology";
    }

    // Xem chi tiết công nghệ
    @GetMapping("/view/{id}")
    public String viewTechnology(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Technology> technology = technologyService.findById(id);

        if (!technology.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy công nghệ!");
            return "redirect:/admin/technology";
        }

        model.addAttribute("technology", technology.get());
        return "admin/technology/technology-view";
    }

    // Xóa mềm công nghệ
    @PostMapping("/delete/{id}")
    public String deleteTechnology(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            technologyService.softDeleteTechnology(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa công nghệ thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/technology";
    }

    // Hiển thị danh sách công nghệ đã xóa
    @GetMapping("/deleted")
    public String listDeletedTechnologies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model) {

        Page<Technology> deletedTechnologies = technologyService.getDeletedTechnologies(page, size);

        model.addAttribute("technologies", deletedTechnologies);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedTechnologies.getTotalPages());
        model.addAttribute("totalElements", deletedTechnologies.getTotalElements());

        return "admin/technology/technology-deleted";
    }

    // Khôi phục công nghệ
    @PostMapping("/restore/{id}")
    public String restoreTechnology(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            technologyService.restoreTechnology(id);
            redirectAttributes.addFlashAttribute("successMessage", "Khôi phục công nghệ thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/technology/deleted";
    }

    // Xóa vĩnh viễn công nghệ
    @PostMapping("/permanent-delete/{id}")
    public String permanentDeleteTechnology(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            technologyService.permanentDeleteTechnology(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa vĩnh viễn công nghệ thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/technology/deleted";
    }

    // API kiểm tra tên công nghệ đã tồn tại
    @GetMapping("/check-name")
    @ResponseBody
    public boolean checkNameExists(@RequestParam String name, @RequestParam(required = false) Integer id) {
        if (id != null) {
            return technologyService.isNameExistsExceptId(name, id);
        }
        return technologyService.isNameExists(name);
    }
}