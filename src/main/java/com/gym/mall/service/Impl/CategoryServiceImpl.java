package com.gym.mall.service.Impl;

import com.gym.mall.converter.CategoryConverter;
import com.gym.mall.domain.entity.Category;
import com.gym.mall.Repository.CategoryRepository;
import com.gym.mall.domain.dto.CategoryDTO;
import com.gym.mall.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Long addCategory(CategoryDTO categoryDTO) {
        List<Category> existingList = categoryRepository.findByName(categoryDTO.getName());
        if (!CollectionUtils.isEmpty(existingList)) {
            throw new IllegalStateException("分类名称 '" + categoryDTO.getName() + "' 已存在");
        }
        Category category = categoryRepository.save(CategoryConverter.converterCategory(categoryDTO));
        log.info("新增分类成功, id: {}", category.getId());
        return category.getId();
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("分类不存在, id: " + id));
        return CategoryConverter.converterCategory(category);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        return categories.stream()
                .map(CategoryConverter::converterCategory)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(Long id, String name, String description, Integer sortOrder) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("分类不存在, id: " + id));
        
        if (name != null && !name.isEmpty()) {
            category.setName(name);
        }
        if (description != null) {
            category.setDescription(description);
        }
        if (sortOrder != null) {
            category.setSortOrder(sortOrder);
        }
        
        Category saved = categoryRepository.save(category);
        log.info("更新分类成功, id: {}", saved.getId());
        return CategoryConverter.converterCategory(saved);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("分类不存在, id: " + id));
        categoryRepository.deleteById(id);
        log.info("删除分类成功, id: {}", id);
    }
}