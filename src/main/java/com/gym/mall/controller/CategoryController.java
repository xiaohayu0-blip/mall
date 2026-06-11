package com.gym.mall.controller;

import com.gym.mall.domain.dto.CategoryDTO;
import com.gym.mall.service.CategoryService;
import com.gym.mall.validator.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品分类", description = "分类的增删改查")
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "新增分类")
    @PostMapping
    public Response<Long> addCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        return Response.newSuccess(categoryService.addCategory(categoryDTO));
    }

    @Operation(summary = "根据ID查询分类")
    @GetMapping("/{id}")
    public Response<CategoryDTO> getCategoryById(@Parameter(description = "分类ID") @PathVariable Long id) {
        return Response.newSuccess(categoryService.getCategoryById(id));
    }

    @Operation(summary = "查询所有分类")
    @GetMapping("/list")
    public Response<List<CategoryDTO>> getAllCategories() {
        return Response.newSuccess(categoryService.getAllCategories());
    }

    @Operation(summary = "更新分类信息", description = "仅传入需要修改的字段")
    @PutMapping("/{id}")
    public Response<CategoryDTO> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Parameter(description = "分类名称") @RequestParam(required = false) String name,
            @Parameter(description = "分类描述") @RequestParam(required = false) String description,
            @Parameter(description = "排序权重") @RequestParam(required = false) Integer sortOrder) {
        return Response.newSuccess(categoryService.updateCategory(id, name, description, sortOrder));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Response<String> deleteCategory(@Parameter(description = "分类ID") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Response.newSuccess("删除成功");
    }
}