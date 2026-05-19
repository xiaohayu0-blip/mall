package com.gym.mall.service;

import com.gym.mall.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {

    Long addCategory(CategoryDTO categoryDTO);

    CategoryDTO getCategoryById(Long id);

    List<CategoryDTO> getAllCategories();

    CategoryDTO updateCategory(Long id, String name, String description, Integer sortOrder);

    void deleteCategory(Long id);
}