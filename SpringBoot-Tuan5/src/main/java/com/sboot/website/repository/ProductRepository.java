package com.sboot.website.repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
//Tìm Kiếm theo nội dung tên
List<Product> findByProductNameContaining(String name);
//Tìm kiếm và Phân trang
Page<Product> findByProductNameContaining(String name,Pageable
pageable);
Optional<Product> findByProductName(String name);
Optional<Product> findByCreateDate(Date createAt);
}