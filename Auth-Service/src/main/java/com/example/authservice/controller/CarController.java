package com.example.authservice.controller;

import com.example.authservice.dto.CarRequest;
import com.example.authservice.dto.CarResponse;
import com.example.authservice.model.entity.Car;
import com.example.authservice.service.CarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    /**
     * Erstellt oder aktualisiert das Fahrzeug des authentifizierten Users.
     * PUT: Upsert-Verhalten (Create wenn keines existiert, sonst Update).
     */
    @PostMapping("/profile/car")
    public ResponseEntity<CarResponse> createOrUpdateCar(@RequestBody CarRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Konvertiere die FuelType-String in das Enum
        Car.FuelType fuelType;
        try {
            fuelType = Car.FuelType.valueOf(request.getFuelType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }

        Car car = carService.createOrUpdateCar(
                username,
                request.getBrand(),
                request.getModel(),
                request.getAverageConsumptionLPer100km(),
                fuelType
        );

        return ResponseEntity.ok(CarResponse.fromEntity(car));
    }

    /**
     * Liefert das Fahrzeug des authentifizierten Users.
     */
    @GetMapping("/profile/car")
    public ResponseEntity<CarResponse> getCar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<Car> car = carService.getCarForUser(username);
        if (car.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(CarResponse.fromEntity(car.get()));
    }

    /**
     * Liefert das Fahrzeug eines Users über die userId.
     */
    @GetMapping("/profile/car/{userId}")
    public ResponseEntity<CarResponse> getCarByUserId(@PathVariable Long userId) {
        Optional<Car> car = carService.getCarForUser(userId);
        if (car.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(CarResponse.fromEntity(car.get()));
    }
}
