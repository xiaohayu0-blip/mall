package com.gym.mall.controller;

import com.gym.mall.domain.dto.CategoryDTO;
import com.gym.mall.service.CategoryService;
import com.gym.mall.validator.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Response<Long> addCategory(@RequestBody CategoryDTO categoryDTO) {
        return Response.newSuccess(categoryService.addCategory(categoryDTO));
    }

    @GetMapping("/{id}")
    public Response<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return Response.newSuccess(categoryService.getCategoryById(id));
    }

    @GetMapping("/list")
    public Response<List<CategoryDTO>> getAllCategories() {
        return Response.newSuccess(categoryService.getAllCategories());
    }

    @PutMapping("/{id}")
    public Response<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer sortOrder) {
        return Response.newSuccess(categoryService.updateCategory(id, name, description, sortOrder));
    }

    @DeleteMapping("/{id}")
    public Response<String> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return Response.newSuccess("删除成功");
        } catch (Exception e) {
            return Response.newFail("删除失败");
        }
    }
}