package com.project.code.Controller;

import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Model.CombinedRequest;
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
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ServiceClass serviceClass;

    /**
     * Update existing inventory and product
     */
    @PutMapping
    public Map<String, String> updateInventory(@RequestBody CombinedRequest combinedRequest) {
        Map<String, String> response = new HashMap<>();
        try {
            Product product = combinedRequest.getProduct();
            Inventory inventory = combinedRequest.getInventory();

            // Validate product ID
            if (!serviceClass.validateProductId(product.getId())) {
                response.put("message", "Invalid product ID");
                return response;
            }

            // Update Inventory if exists
            Inventory existingInventory = serviceClass.getInventoryId(inventory);
            if (existingInventory != null) {
                existingInventory.setStockLevel(inventory.getStockLevel());
                inventoryRepository.save(existingInventory);

                productRepository.save(product); // Update product
                response.put("message", "Successfully updated product");
            } else {
                response.put("message", "No data available");
            }
        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity error: " + e.getMessage());
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    /**
     * Save new inventory record
     */
    @PostMapping
    public Map<String, String> saveInventory(@RequestBody Inventory inventory) {
        Map<String, String> response = new HashMap<>();
        try {
            if (!serviceClass.validateInventory(inventory)) {
                response.put("message", "Inventory data already present");
            } else {
                inventoryRepository.save(inventory);
                response.put("message", "Inventory data saved successfully");
            }
        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity error: " + e.getMessage());
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    /**
     * Get all products for a store
     */
    @GetMapping("/{storeId}")
    public Map<String, Object> getAllProducts(@PathVariable Long storeId) {
        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findProductsByStoreId(storeId);
        response.put("products", products);
        return response;
    }

    /**
     * Filter products by category and/or name for a store
     */
    @GetMapping("filter/{category}/{name}/{storeId}")
    public Map<String, Object> getProductName(
            @PathVariable String category,
            @PathVariable String name,
            @PathVariable Long storeId) {

        Map<String, Object> response = new HashMap<>();
        List<Product> products;

        if ("null".equalsIgnoreCase(category) && !"null".equalsIgnoreCase(name)) {
            // Filter by name only
            products = productRepository.findByNameLike(storeId, name);
        } else if (!"null".equalsIgnoreCase(category) && "null".equalsIgnoreCase(name)) {
            // Filter by category only
            products = productRepository.findByCategoryAndStoreId(storeId, category);
        } else if (!"null".equalsIgnoreCase(category) && !"null".equalsIgnoreCase(name)) {
            // Filter by both name and category
            products = productRepository.findByNameAndCategory(storeId, name, category);
        } else {
            // Both null, return empty
            products = List.of();
        }

        response.put("product", products);
        return response;
    }

    /**
     * Search products by name within a store
     */
    @GetMapping("search/{name}/{storeId}")
    public Map<String, Object> searchProduct(@PathVariable String name, @PathVariable Long storeId) {
        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findByNameLike(storeId, name);
        response.put("product", products);
        return response;
    }

    /**
     * Remove product and corresponding inventory by product ID
     */
    @DeleteMapping("/{id}")
    public Map<String, String> removeProduct(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        if (!serviceClass.validateProductId(id)) {
            response.put("message", "Product not present in database");
            return response;
        }

        // Delete inventory records associated with this product
        inventoryRepository.deleteByProductId(id);
        response.put("message", "Product deleted successfully");
        return response;
    }

    /**
     * Validate if a product has sufficient stock in a store
     */
    @GetMapping("validate/{quantity}/{storeId}/{productId}")
    public boolean validateQuantity(
            @PathVariable Integer quantity,
            @PathVariable Long storeId,
            @PathVariable Long productId) {

        Inventory inventory = inventoryRepository.findByProductIdAndStoreId(productId, storeId);
        return inventory != null && inventory.getStockLevel() >= quantity;
    }
}