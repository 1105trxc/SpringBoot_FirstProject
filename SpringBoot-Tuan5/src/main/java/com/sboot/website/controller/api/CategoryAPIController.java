package com.sboot.website.controller.api;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import com.sboot.website.dto.ApiResponse;
import com.sboot.website.entity.Category;
import com.sboot.website.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/category")
@RequiredArgsConstructor
public class CategoryAPIController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAllCategory() {
        return new ResponseEntity<>(
            new ApiResponse(true, "Thành công", categoryService.findAll()),
            HttpStatus.OK
        );
    }

    @PostMapping(path = "/getCategory")
    public ResponseEntity<?> getCategory(@Validated @RequestParam("id") Integer id) {
        Optional<Category> category = categoryService.findById(id);
        if (category.isPresent()) {
            return new ResponseEntity<>(new ApiResponse(true, "Thành công", category.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse(false, "Không tìm thấy", null), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/addCategory")
    public ResponseEntity<?> addCategory(@Validated @RequestParam("categoryName") String categoryName,
                                         @RequestParam(value = "image", required = false) MultipartFile image) {

        Optional<Category> optCategory = categoryService.findByCategoryName(categoryName);
        if (optCategory.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ApiResponse(false, "Category đã tồn tại trong hệ thống", null));
        }

        Category category = new Category();
        category.setCategoryName(categoryName);

        // Tạm thời: lưu tên file vào field images (chưa xử lý lưu file)
        if (image != null && !image.isEmpty()) {
            category.setImages(StringUtils.cleanPath(image.getOriginalFilename()));
        }

        categoryService.save(category);
        return new ResponseEntity<>(new ApiResponse(true, "Thêm thành công", category), HttpStatus.OK);
    }

    @PutMapping(path = "/updateCategory")
    public ResponseEntity<?> updateCategory(@Validated @RequestParam("categoryId") Integer categoryId,
                                            @Validated @RequestParam("categoryName") String categoryName,
                                            @RequestParam(value = "icon", required = false) MultipartFile icon,
                                            @RequestParam(value = "image", required = false) String image) {

        Optional<Category> optCategory = categoryService.findById(categoryId);
        if (optCategory.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "Không tìm thấy Category", null), HttpStatus.BAD_REQUEST);
        }

        Category category = optCategory.get();
        category.setCategoryName(categoryName);

        if (icon != null && !icon.isEmpty()) {
            // Xử lý lưu file và gán tên file cho images
            String fileName = StringUtils.cleanPath(icon.getOriginalFilename());
            category.setImages(fileName);
            // Thêm đoạn lưu file thực tế nếu cần
        } else if (image != null && !image.isEmpty()) {
            category.setImages(image);
        }

        categoryService.save(category);
        return new ResponseEntity<>(new ApiResponse(true, "Cập nhật thành công", category), HttpStatus.OK);
    }

    @DeleteMapping(path = "/deleteCategory")
    public ResponseEntity<?> deleteCategory(@Validated @RequestParam("categoryId") Integer categoryId){
        Optional<Category> optCategory = categoryService.findById(categoryId);
        if (optCategory.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "Không tìm thấy Category", null), HttpStatus.BAD_REQUEST);
        }
        categoryService.delete(optCategory.get());
        return new ResponseEntity<>(new ApiResponse(true, "Xóa thành công", optCategory.get()), HttpStatus.OK);
    }
}