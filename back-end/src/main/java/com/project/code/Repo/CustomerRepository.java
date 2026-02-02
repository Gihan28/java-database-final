package com.project.code.Repo;


import com.project.code.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Find a customer by their email
    Customer findByEmail(String email);

    // Find a customer by their ID
    Customer findById(long id); // Note: JpaRepository already provides findById(Long id), but this can be used for direct access
}