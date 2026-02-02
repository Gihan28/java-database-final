package com.project.code.Controller;


import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ServiceClass serviceClass;

    @Autowired
    public ProductController(ProductRepository productRepository,
                             InventoryRepository inventoryRepository,
                             ServiceClass serviceClass) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.serviceClass = serviceClass;
    }

    /**
     * Add a new product
     */
    @PostMapping
    public Map<String, String> addProduct(@RequestBody Product product) {
        Map<String, String> response = new HashMap<>();
        try {
            if (!serviceClass.validateProduct(product)) {
                response.put("message", "Product already exists");
                return response;
            }
            productRepository.save(product);
            response.put("message", "Product added successfully");
        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity violation: " + e.getMessage());
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    /**
     * Get product by ID
     */
    @GetMapping("/product/{id}")
    public Map<String, Object> getProductById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        productRepository.findById(id).ifPresent(product -> response.put("products", product));
        return response;
    }

    /**
     * Update an existing product
     */
    @PutMapping
    public Map<String, String> updateProduct(@RequestBody Product product) {
        Map<String, String> response = new HashMap<>();
        try {
            productRepository.save(product);
            response.put("message", "Product updated successfully");
        } catch (Exception e) {
            response.put("message", "Error updating product: " + e.getMessage());
        }
        return response;
    }

    /**
     * Filter products by name and category
     */
    @GetMapping("/category/{name}/{category}")
    public Map<String, Object> filterByCategoryProduct(
            @PathVariable String name,
            @PathVariable String category) {

        Map<String, Object> response = new HashMap<>();
        List<Product> products;

        if ("null".equalsIgnoreCase(name) && !"null".equalsIgnoreCase(category)) {
            products = productRepository.findByCategory(category);
        } else if (!"null".equalsIgnoreCase(name) && "null".equalsIgnoreCase(category)) {
            products = productRepository.findProductBySubName(name);
        } else if (!"null".equalsIgnoreCase(name) && !"null".equalsIgnoreCase(category)) {
            products = productRepository.findProductBySubNameAndCategory(name, category);
        } else {
            products = List.of();
        }

        response.put("products", products);
        return response;
    }

    /**
     * List all products
     */
    @GetMapping
    public Map<String, Object> listProduct() {
        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findAll();
        response.put("products", products);
        return response;
    }

    /**
     * Get products by category and store ID
     */
    @GetMapping("filter/{category}/{storeId}")
    public Map<String, Object> getProductByCategoryAndStoreId(
            @PathVariable String category,
            @PathVariable Long storeId) {

        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findProductByCategory(category, storeId);
        response.put("product", products);
        return response;
    }

    /**
     * Delete a product by ID (also removes inventory)
     */
    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        if (!serviceClass.validateProductId(id)) {
            response.put("message", "Product not present in database");
            return response;
        }

        inventoryRepository.deleteByProductId(id);
        productRepository.deleteById(id);
        response.put("message", "Product deleted successfully");
        return response;
    }

    /**
     * Search products by name
     */
    @GetMapping("/searchProduct/{name}")
    public Map<String, Object> searchProduct(@PathVariable String name) {
        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findProductBySubName(name);
        response.put("products", products);
        return response;
    }
}