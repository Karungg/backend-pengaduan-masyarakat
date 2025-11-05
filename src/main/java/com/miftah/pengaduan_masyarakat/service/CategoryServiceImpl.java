package com.miftah.pengaduan_masyarakat.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miftah.pengaduan_masyarakat.dto.CategoryRequest;
import com.miftah.pengaduan_masyarakat.dto.CategoryResponse;
import com.miftah.pengaduan_masyarakat.exception.ResourceNotFoundException;
import com.miftah.pengaduan_masyarakat.exception.ValidationException;
import com.miftah.pengaduan_masyarakat.model.Category;
import com.miftah.pengaduan_masyarakat.repository.CategoryRepository;
import com.miftah.pengaduan_masyarakat.repository.ComplaintRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ComplaintRepository complaintRepository;
    private final MessageSource messageSource;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Attempting to create new category with name: {}", request.getName());
        validateCategoryNameUniqueness(request.getName(), null);

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());

        return convertToCategoryResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        log.debug("Fetching category with ID: {}", id);
        Category category = findCategoryByIdOrThrow(id);
        return convertToCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        log.info("Attempting to update category with ID: {}", id);
        Category categoryToUpdate = findCategoryByIdOrThrow(id);

        if (request.getName() != null && !request.getName().equals(categoryToUpdate.getName())) {
            validateCategoryNameUniqueness(request.getName(), id);
            categoryToUpdate.setName(request.getName());
        }

        categoryToUpdate.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(categoryToUpdate);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());

        return convertToCategoryResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Attempting to delete category with ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            log.warn("Deletion failed. Category with ID {} not found.", id);
            throw createResourceNotFoundException("category.notfound.id", id);
        }

        if (complaintRepository.existsByCategoryId(id)) {
            log.warn("Deletion failed. Category with ID {} is still referenced by complaints.", id);
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("category.delete.referenced", null,
                    "Kategori ini tidak bisa dihapus karena masih digunakan oleh pengaduan.", locale);
            throw new ValidationException(message, Map.of("id", List.of(message)));
        }

        categoryRepository.deleteById(id);
        log.info("Category deleted successfully with ID: {}", id);
    }

    private Category findCategoryByIdOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> createResourceNotFoundException("category.notfound.id", id));
    }

    private void validateCategoryNameUniqueness(String name, UUID currentId) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean isCreating = (currentId == null);

        if (isCreating && categoryRepository.existsByName(name)) {
            String message = messageSource.getMessage("category.name.unique", new Object[] { name },
                    "Nama kategori sudah ada.", locale);
            log.warn("Validation failed: {}", message);
            throw new ValidationException(message, Map.of("name", List.of(message)));
        }

        if (!isCreating && categoryRepository.existsByNameAndIdNot(name, currentId)) {
            String message = messageSource.getMessage("category.name.unique", new Object[] { name },
                    "Nama kategori sudah ada.", locale);
            log.warn("Validation failed: {}", message);
            throw new ValidationException(message, Map.of("name", List.of(message)));
        }
    }

    private CategoryResponse convertToCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    private ResourceNotFoundException createResourceNotFoundException(String messageKey, Object... args) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(messageKey, args, "Resource not found", currentLocale);
        return new ResourceNotFoundException(message);
    }
}
