package com.miftah.pengaduan_masyarakat.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.miftah.pengaduan_masyarakat.dto.GenericResponse;
import com.miftah.pengaduan_masyarakat.dto.CategoryRequest;
import com.miftah.pengaduan_masyarakat.dto.CategoryResponse;
import com.miftah.pengaduan_masyarakat.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<GenericResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        log.info("Received request to create category with name: {}", request.getName());
        CategoryResponse createdCategory = categoryService.createCategory(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCategory.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(GenericResponse.created(createdCategory));
    }

    @GetMapping
    public ResponseEntity<GenericResponse<List<CategoryResponse>>> getAllCategories() {
        log.info("Received request to fetch all categories");

        List<CategoryResponse> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(GenericResponse.ok(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<CategoryResponse>> getCategoryById(@PathVariable UUID id) {
        log.info("Received request to fetch category with ID: {}", id);

        CategoryResponse category = categoryService.getCategoryById(id);

        return ResponseEntity.ok(GenericResponse.ok(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<CategoryResponse>> updateCategory(@PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        log.info("Received request to update category with ID: {}", id);

        CategoryResponse updatedCategory = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(GenericResponse.ok(updatedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteCategory(@PathVariable UUID id) {
        log.info("Received request to delete category with ID: {}", id);

        categoryService.deleteCategory(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.noContent());
    }
}
