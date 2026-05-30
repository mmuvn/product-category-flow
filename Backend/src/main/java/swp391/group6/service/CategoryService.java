package swp391.group6.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import swp391.group6.dto.CategoryRequest;
import swp391.group6.dto.CategoryResponse;
import swp391.group6.entity.Category;
import swp391.group6.repository.CategoryRepository;
import swp391.group6.repository.ProductRepository;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse getCategory(Long id) {
        return toResponse(findCategory(id));
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        String name = normalizeName(request.getName());
        Long parentId = request.getParentId();
        validateParent(parentId, null);
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(trimToNull(request.getDescription()));
        category.setParentId(parentId);
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategory(id);
        String name = normalizeName(request.getName());
        Long parentId = request.getParentId();
        validateParent(parentId, id);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }

        category.setName(name);
        category.setDescription(trimToNull(request.getDescription()));
        category.setParentId(parentId);
        return toResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        Category category = findCategory(id);
        if (productRepository.existsByCategoryId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category has products");
        }
        if (categoryRepository.existsByParentId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category has child categories");
        }
        categoryRepository.delete(category);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private void validateParent(Long parentId, Long currentCategoryId) {
        if (parentId == null) {
            return;
        }
        if (parentId.equals(currentCategoryId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category cannot be its own parent");
        }
        if (!categoryRepository.existsById(parentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent category not found");
        }
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setParentId(category.getParentId());
        return response;
    }

    private String normalizeName(String name) {
        String normalized = trimToNull(name);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
