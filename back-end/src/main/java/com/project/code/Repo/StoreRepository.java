package com.project.code.Repo;


import com.project.code.Model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // Retrieve a store by its ID
    Store findById(long id); // Optional: JpaRepository already provides findById(Long id) returning Optional<Store>

    // Retrieve stores whose name contains a given substring (case-insensitive)
    @Query("SELECT s FROM Store s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :pname, '%'))")
    List<Store> findBySubName(String pname);
}