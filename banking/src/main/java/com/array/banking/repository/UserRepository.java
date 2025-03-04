package com.array.banking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.array.banking.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    // TODO: More efficient way to get a random user
    @Query(value = "SELECT * FROM users ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<User> findRandomUser();
}
