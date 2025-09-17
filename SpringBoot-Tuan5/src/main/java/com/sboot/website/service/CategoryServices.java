package com.sboot.website.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sboot.website.entity.Category;

public interface CategoryServices {

    // Lấy tất cả
    List<Category> findAll();

    // Lấy tất cả có phân trang
    Page<Category> findAll(Pageable pageable);

    // Tìm theo id
    Optional<Category> findById(Integer id);

    // Lưu hoặc update
    Category save(Category entity);

    // Xóa theo id
    void deleteById(Integer id);

    // Đếm số lượng
    long count();

    // Tìm theo tên chứa chuỗi (không phân trang)
    List<Category> findByCategoryNameContaining(String name);

    // Tìm theo tên có phân trang
    Page<Category> findByCategoryNameContaining(String name, Pageable pageable);

    // Tìm theo Example
    <S extends Category> Optional<S> findOne(Example<S> example);
}
