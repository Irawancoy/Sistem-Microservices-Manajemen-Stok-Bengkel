package com.microservices.smmsb_user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.microservices.smmsb_user_service.model.User;

public interface UserRepository extends JpaRepository<User, Long> ,JpaSpecificationExecutor<User> {
   Optional<User> findByUsername(String username);
   Optional<User> findByEmail(String email);
   boolean existsByUsername(String username);
   boolean existsByEmail(String email); 
}
