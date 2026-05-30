package swp391.group6.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import swp391.group6.dto.ProductRequest;
import swp391.group6.dto.ProductResponse;
import swp391.group6.entity.Category;
import swp391.group6.entity.Product;
import swp391.group6.repository.CategoryRepository;
import swp391.group6.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductResponse> listProducts(String keyword, Long categoryId, Boolean status) {
        String normalizedKeyword = keyword == null ? null : keyword.trim().toLowerCase();
        return productRepository.findAll().stream()
                .filter(product -> normalizedKeyword == null
                        || normalizedKeyword.isEmpty()
                        || containsIgnoreCase(product.getName(), normalizedKeyword)
                        || containsIgnoreCase(product.getSku(), normalizedKeyword))
                .filter(product -> categoryId == null || categoryId.equals(product.getCategoryId()))
                .filter(product -> status == null || status.equals(product.isStatus()))
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getProduct(Long id) {
        return toResponse(findProduct(id));
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        if (productRepository.existsBySkuIgnoreCase(product.getSku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product SKU already exists");
        }
        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);
        String normalizedSku = normalizeSku(request.getSku());
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(normalizedSku, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product SKU already exists");
        }
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    public void deactivateProduct(Long id) {
        Product product = findProduct(id);
        product.setStatus(false);
        productRepository.save(product);
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setCategoryId(request.getCategoryId());
        validateCategory(product.getCategoryId());
        product.setName(normalizeName(request.getName()));
        product.setPrice(normalizePrice(request.getPrice()));
        product.setStock(normalizeStock(request.getStock()));
        product.setStatus(request.getStatus() == null || request.getStatus());
        product.setSku(normalizeSku(request.getSku()));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void validateCategory(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found");
        }
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setCategoryId(product.getCategoryId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setStatus(product.isStatus());
        response.setSku(product.getSku());
        return response;
    }

    private String normalizeName(String name) {
        String normalized = trimToNull(name);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        return normalized;
    }

    private String normalizeSku(String sku) {
        String normalized = trimToNull(sku);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product SKU is required");
        }
        return normalized;
    }

    private BigDecimal normalizePrice(BigDecimal price) {
        if (price == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product price is required");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product price cannot be negative");
        }
        return price;
    }

    private int normalizeStock(Integer stock) {
        int normalizedStock = stock == null ? 0 : stock;
        if (normalizedStock < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product stock cannot be negative");
        }
        return normalizedStock;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
