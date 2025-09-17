package com.sboot.website.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.sboot.website.entity.Category;

public interface CategoryServices {
    List<Category> findAll();

    List<Category> findByCategoryNameContaining(String name);

    Page<Category> findByCategoryNameContaining(String name, Pageable pageable);

    Category save(Category entity);

    void deleteById(Integer id);  // giữ Integer, bỏ int
}
