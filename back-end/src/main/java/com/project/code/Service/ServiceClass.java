package com.project.code.Service;


import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Model.OrderItem;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceClass {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public ServiceClass(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Checks if an inventory record exists for a given product and store.
     *
     * @param inventory The inventory object to validate
     * @return false if inventory already exists for the product-store pair, true otherwise
     */
    public boolean validateInventory(Inventory inventory) {
        Inventory existingInventory = inventoryRepository.findByProductIdAndStoreId(
                inventory.getProduct().getId(), inventory.getStore().getId());
        return existingInventory == null;
    }

    /**
     * Checks if a product exists by its name.
     *
     * @param product The product to validate
     * @return false if a product with the same name exists, true otherwise
     */
    public boolean validateProduct(Product product) {
        Product existingProduct = productRepository.findByName(product.getName());
        return existingProduct == null;
    }

    /**
     * Validates whether a product exists by its ID.
     *
     * @param id The product ID
     * @return true if product exists, false otherwise
     */
    public boolean validateProductId(long id) {
        return productRepository.findById(id).isPresent();
    }

    /**
     * Fetches the inventory record for a given product and store combination.
     *
     * @param inventory The inventory object to search for
     * @return The found inventory record
     */
    public Inventory getInventoryId(Inventory inventory) {
        return inventoryRepository.findByProductIdAndStoreId(
                inventory.getProduct().getId(), inventory.getStore().getId());
    }
}