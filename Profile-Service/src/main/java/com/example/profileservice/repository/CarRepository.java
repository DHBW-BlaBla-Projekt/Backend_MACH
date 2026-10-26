package com.example.profileservice.repository;

import com.example.profileservice.model.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    /**
     * Finde alle Fahrzeuge eines Users.
     */
    List<Car> findByUserId(Long userId);

    /**
     * Prüft ob ein User bereits ein Fahrzeug hinterlegt hat (für Legacy-Kompatibilität).
     */
    boolean existsByUserId(Long userId);
}
