package com.sboot.website.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
@RequestMapping("admin/categories")
public class CategoryController {

    @Autowired
    private CategoryServices categoryService;

    @GetMapping("add")
    public String add(ModelMap model) { 
        Category category = new Category();
        model.addAttribute("category", category);
        return "admin/categories/add";
    }


    @RequestMapping("")
    public String list(ModelMap model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("delete/{categoryId}")
    public ModelAndView delete(ModelMap model, @PathVariable("categoryId") int categoryId) {
        categoryService.deleteById(categoryId);
        model.addAttribute("message", "Category is deleted!");
        return new ModelAndView("redirect:/admin/categories", model);
    }

    @RequestMapping("search")
    public String search(ModelMap model, @RequestParam(name="name", required=false) String name) {
        List<Category> list = (name != null && !name.isEmpty())
                ? categoryService.findByCategoryNameContaining(name)
                : categoryService.findAll();

        model.addAttribute("categories", list);
        return "admin/categories/search";
    }
    
    @PostMapping("saveOrUpdate")
    public ModelAndView saveOrUpdate(ModelMap model, 
            @ModelAttribute("category") Category category) {

        categoryService.save(category); // gọi service để lưu

        model.addAttribute("message", "Category is saved!");
        return new ModelAndView("redirect:/admin/categories", model);
    }
}

