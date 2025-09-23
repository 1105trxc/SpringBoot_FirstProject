package com.sboot.website.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="categories")
@NamedQuery(name="Category.findAll", query="SELECT c FROM Category c")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // đổi từ int -> Integer để khớp Repository

    @Column(name="categoryName", columnDefinition = "NVARCHAR(255)")
    private String categoryName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String images;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String description;

    // Trường dùng cho Thymeleaf để phân biệt edit/add, không lưu DB
    @Transient
    private boolean isEdit = false;

    // Giữ các getter/setter thủ công nếu bạn đang dùng ngoài Lombok
    public boolean getIsEdit() { return isEdit; }
    public void setIsEdit(boolean isEdit) { this.isEdit = isEdit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Nếu muốn sử dụng tên categoryId cho template/controller
    @Transient
    public Integer getCategoryId() {
        return id;
    }
    public void setCategoryId(Integer categoryId) {
        this.id = categoryId;
    }
}