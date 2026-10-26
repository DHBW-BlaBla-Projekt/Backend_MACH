package com.example.profileservice.repository;

import com.example.profileservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Datenzugriffschicht für User */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
