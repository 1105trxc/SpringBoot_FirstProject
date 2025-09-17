package com.sboot.website.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.sboot.website.entity.Category;
import com.sboot.website.service.CategoryServices;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryServices categoryService;

    // Hiển thị tất cả category (danh sách không phân trang, dùng cho list.html)
    @GetMapping({"", "/list"})
    public String list(ModelMap model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    // Hiển thị form thêm mới category
    @GetMapping("/add")
    public String add(ModelMap model) {
        Category category = new Category();
        category.setIsEdit(false); // Nếu có trường này, để phân biệt khi dùng form chung
        model.addAttribute("category", category);
        return "admin/categories/AddOrEdit"; // dùng form chung cho add/edit
    }

    // Hiển thị form chỉnh sửa category
    @GetMapping("/edit/{categoryId}")
    public String edit(ModelMap model, @PathVariable("categoryId") int categoryId) {
        Optional<Category> categoryOpt = categoryService.findById(categoryId);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            category.setIsEdit(true); // Nếu có trường này, để phân biệt khi dùng form chung
            model.addAttribute("category", category);
            return "admin/categories/AddOrEdit";
        }
        model.addAttribute("message", "Category not found!");
        return "redirect:/admin/categories/searchpaginated";
    }

    // Lưu hoặc cập nhật category (dùng cho cả thêm mới và chỉnh sửa)
    @PostMapping("/saveOrUpdate")
    public ModelAndView saveOrUpdate(ModelMap model,
                                     @ModelAttribute("category") Category category) {
        categoryService.save(category);
        model.addAttribute("message", "Category is saved!");
        return new ModelAndView("redirect:/admin/categories/searchpaginated", model);
    }

    // Xóa category và forward về searchpaginated để cập nhật danh sách
    @GetMapping("/delete2/{categoryId}")
    public ModelAndView deleteForward(ModelMap model, @PathVariable("categoryId") int categoryId) {
        categoryService.deleteById(categoryId);
        model.addAttribute("message", "Category is deleted!");
        return new ModelAndView("forward:/admin/categories/searchpaginated", model);
    }

    // Xóa category và redirect về list (nếu muốn dùng phiên bản cũ)
    @GetMapping("/delete/{categoryId}")
    public ModelAndView delete(ModelMap model, @PathVariable("categoryId") int categoryId) {
        categoryService.deleteById(categoryId);
        model.addAttribute("message", "Category is deleted!");
        return new ModelAndView("redirect:/admin/categories/list", model);
    }

    // Tìm kiếm category theo tên (không phân trang)
    @RequestMapping("/search")
    public String search(ModelMap model, @RequestParam(name = "name", required = false) String name) {
        List<Category> list;
        if (StringUtils.hasText(name)) {
            list = categoryService.findByCategoryNameContaining(name);
        } else {
            list = categoryService.findAll();
        }
        model.addAttribute("categories", list);
        model.addAttribute("name", name);
        return "admin/categories/search";
    }

    // Tìm kiếm category theo tên (có phân trang)
    @RequestMapping("/searchpaginated")
    public String searchPaginated(ModelMap model,
                                  @RequestParam(name = "name", required = false) String name,
                                  @RequestParam(name = "page", required = false) Optional<Integer> page,
                                  @RequestParam(name = "size", required = false) Optional<Integer> size) {

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(3);

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by("name"));
        Page<Category> resultPage;

        if (StringUtils.hasText(name)) {
            resultPage = categoryService.findByCategoryNameContaining(name, pageable);
            model.addAttribute("name", name);
        } else {
            resultPage = categoryService.findAll(pageable);
        }

        int totalPages = resultPage.getTotalPages();
        int start = Math.max(1, currentPage - 2);
        int end = Math.min(currentPage + 2, totalPages);

        if (totalPages > 0) {
            if (end > totalPages) end = totalPages;
            if (end - start < 4) start = Math.max(1, end - 4);
        }

        List<Integer> pageNumbers = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("categoryPage", resultPage);
        return "admin/categories/searchpaginated";
    }

    // Xem chi tiết category (nếu dùng chức năng view)
    @GetMapping("/view/{categoryId}")
    public String view(ModelMap model, @PathVariable("categoryId") int categoryId) {
        Optional<Category> categoryOpt = categoryService.findById(categoryId);
        if (categoryOpt.isPresent()) {
            model.addAttribute("category", categoryOpt.get());
            return "admin/categories/view";
        }
        model.addAttribute("message", "Category not found!");
        return "redirect:/admin/categories/searchpaginated";
    }
}