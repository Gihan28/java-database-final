package com.project.code.Controller;

import com.project.code.Model.Review;
import com.project.code.Model.Customer;
import com.project.code.Repo.ReviewRepository;
import com.project.code.Repo.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public ReviewController(ReviewRepository reviewRepository,
                            CustomerRepository customerRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Get reviews for a specific product in a store
     */
@GetMapping("/{storeId}/{productId}")
public Map<String, Object> getReviews(@PathVariable Long storeId,
                                      @PathVariable Long productId) {
    Map<String, Object> response = new HashMap<>();

    // Fetch reviews for the given store and product
    List<Review> reviews = reviewRepository.findByStoreIdAndProductId(storeId, productId);
    List<Map<String, Object>> reviewList = new ArrayList<>();

    for (Review review : reviews) {
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("comment", review.getComment());
        reviewData.put("rating", review.getRating());

        // Fetch customer name safely using Optional
        Optional<Customer> customerOpt = customerRepository.findById(review.getCustomerId());
        String customerName = customerOpt.map(Customer::getName).orElse("Unknown");
        reviewData.put("customerName", customerName);

        reviewList.add(reviewData);
    }

    response.put("reviews", reviewList);
    return response;
}

}