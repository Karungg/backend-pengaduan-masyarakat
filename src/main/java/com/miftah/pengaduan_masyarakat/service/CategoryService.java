package com.miftah.pengaduan_masyarakat.service;

import java.util.List;
import java.util.UUID;

import com.miftah.pengaduan_masyarakat.dto.CategoryRequest;
import com.miftah.pengaduan_masyarakat.dto.CategoryResponse;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse getCategoryById(UUID id);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(UUID id, CategoryRequest request);

    void deleteCategory(UUID id);
}
