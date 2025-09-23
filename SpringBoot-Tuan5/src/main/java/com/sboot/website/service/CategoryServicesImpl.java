package com.sboot.website.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sboot.website.entity.Category;
import com.sboot.website.repository.CategoryRepository;

//khai báo service
@Service
public class CategoryServiceImpl implements ICategoryService{
@Autowired
CategoryRepository categoryRepository;
//source -> Generate Constructor using Field, xóa super()
@Override
public <S extends Category> S save(S entity) {
if(entity.getCategoryId() == null) {
return categoryRepository.save(entity);
}else {
Optional<Category> opt = findById(entity.getCategoryId());
if(opt.isPresent()) {
if (StringUtils.isEmpty(entity.getIcon())) {
entity.setIcon(opt.get().getIcon());
}else {
//lấy lại images cũ
entity.setIcon(entity.getIcon());
}
}
return categoryRepository.save(entity);
}
}
@Override
public Optional<Category> findByCategoryName(String name) {
return categoryRepository.findByCategoryName(name);
}
public CategoryServiceImpl(CategoryRepository categoryRepository) {
this.categoryRepository = categoryRepository;
}
@Override
public List<Category> findAll() {
return categoryRepository.findAll();
}
@Override
public Page<Category> findAll(Pageable pageable) {
return categoryRepository.findAll(pageable);
}
@Override
public List<Category> findAll(Sort sort) {
return categoryRepository.findAll(sort);
}
@Override
public List<Category> findAllById(Iterable<Long> ids) {
return categoryRepository.findAllById(ids);
}
@Override
public Optional<Category> findById(Long id) {
return categoryRepository.findById(id);
}
@Override
public <S extends Category> Optional<S> findOne(Example<S> example) {
return categoryRepository.findOne(example);
}
@Override
public long count() {
return categoryRepository.count();
}
@Override
public void deleteById(Long id) {
categoryRepository.deleteById(id);
}
@Override
public void delete(Category entity) {
categoryRepository.delete(entity);
}
@Override
public List<Category> findByCategoryNameContaining(String name) {
return categoryRepository.findByCategoryNameContaining(name);
}
@Override
public Page<Category> findByCategoryNameContaining(String name, Pageable
pageable) {
return categoryRepository.findByCategoryNameContaining(name, pageable);
}
}