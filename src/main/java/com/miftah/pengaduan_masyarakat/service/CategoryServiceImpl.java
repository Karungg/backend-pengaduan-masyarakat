package com.miftah.pengaduan_masyarakat.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.miftah.pengaduan_masyarakat.dto.CategoryRequest;
import com.miftah.pengaduan_masyarakat.dto.CategoryResponse;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createCategory'");
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCategoryById'");
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllCategories'");
    }

    @Override
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateCategory'");
    }

    @Override
    public void deleteCategory(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteCategory'");
    }

}
