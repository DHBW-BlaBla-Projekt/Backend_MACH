package com.example.authservice.repository;

import com.example.authservice.model.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    /**
     * Finde das Hauptfahrzeug eines Users.
     */
    Optional<Car> findByUserId(Long userId);

    /**
     * Prüft ob ein User bereits ein Fahrzeug hinterlegt hat.
     */
    boolean existsByUserId(Long userId);
}