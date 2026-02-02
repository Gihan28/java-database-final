package com.project.code.Service;

import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.StoreRepository;
import com.project.code.Repo.OrderDetailsRepository;
import com.project.code.Repo.OrderItemRepository;


import com.project.code.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    /**
     * Processes a customer's order: creates OrderDetails, updates Inventory, and saves OrderItems.
     *
     * @param placeOrderRequest DTO containing customer info, store ID, purchased products, and total price.
     */
    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {
        // 1️⃣ Retrieve or create the customer
        Customer customer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());
        if (customer == null) {
            customer = new Customer();
            customer.setName(placeOrderRequest.getCustomerName());
            customer.setEmail(placeOrderRequest.getCustomerEmail());
            customer.setPhone(placeOrderRequest.getCustomerPhone());
            customer = customerRepository.save(customer); // Save new customer
        }

        // 2️⃣ Retrieve the store
        Store store = storeRepository.findById(placeOrderRequest.getStoreId())
                .orElseThrow(() -> new RuntimeException(
                        "Store not found with ID: " + placeOrderRequest.getStoreId()));

        // 3️⃣ Create OrderDetails
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setTotalPrice(placeOrderRequest.getTotalPrice());
        orderDetails.setDate(LocalDateTime.now());
        orderDetails = orderDetailsRepository.save(orderDetails);

        // 4️⃣ Process each purchased product
        List<PurchaseProductDTO> products = placeOrderRequest.getPurchaseProduct();
        for (PurchaseProductDTO productOrder : products) {
            // Fetch Product entity
            Product product = productRepository.findById(productOrder.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found with ID: " + productOrder.getId()));

            // Fetch Inventory for this product in the store
            Inventory inventory = inventoryRepository.findByProductIdAndStoreId(product.getId(), store.getId());
            if (inventory == null) {
                throw new RuntimeException("Inventory not found for product ID: " + product.getId() + " in store ID: " + store.getId());
            }

            // Check stock availability
            if (inventory.getStockLevel() < productOrder.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product ID: " + product.getId());
            }

            // Update inventory stock
            inventory.setStockLevel(inventory.getStockLevel() - productOrder.getQuantity());
            inventoryRepository.save(inventory);

            // Create and save OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(orderDetails);
            orderItem.setProduct(product);
            orderItem.setQuantity(productOrder.getQuantity());
            orderItem.setPrice(productOrder.getPrice());
            orderItemRepository.save(orderItem);
        }
    }
}