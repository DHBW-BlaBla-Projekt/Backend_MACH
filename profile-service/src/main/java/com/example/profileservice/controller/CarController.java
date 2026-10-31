package com.example.profileservice.controller;

import com.example.profileservice.dto.CarRequest;
import com.example.profileservice.dto.CarResponse;
import com.example.profileservice.model.entity.Car;
import com.example.profileservice.repository.UserRepository;
import com.example.profileservice.service.CarService;
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
    private final UserRepository userRepository;

    public CarController(CarService carService, UserRepository userRepository) {
        this.carService = carService;
        this.userRepository = userRepository;
    }

    /**
     * Erstellt ein neues Fahrzeug für den authentifizierten User.
     */
    @PostMapping("/profile/cars")
    public ResponseEntity<CarResponse> createCar(@RequestBody CarRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        var user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden: " + username));

        Car.FuelType fuelType;
        try {
            fuelType = Car.FuelType.valueOf(request.getFuelType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }

        Car car = carService.createCar(
                user.getId(),
                request.getName() != null ? request.getName() : (request.getBrand() + " " + request.getModel()),
                request.getBrand(),
                request.getModel(),
                request.getLicensePlate(),
                request.getAverageConsumptionLPer100km(),
                fuelType
        );

        return ResponseEntity.ok(CarResponse.fromEntity(car));
    }

    /**
     * Liefert alle Fahrzeuge des authentifizierten Users.
     */
    @GetMapping("/profile/cars")
    public ResponseEntity<List<CarResponse>> getCars() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<Car> cars = carService.getAllCarsByUsername(username);
        return ResponseEntity.ok(cars.stream().map(CarResponse::fromEntity).collect(Collectors.toList()));
    }

    /**
     * Liefert ein spezifisches Fahrzeug eines Users.
     */
    @GetMapping("/profile/cars/{carId}")
    public ResponseEntity<CarResponse> getCar(@PathVariable Long carId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<Car> carOpt = carService.getCarById(carId);
        
        if (carOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Car car = carOpt.get();
        

        var user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden: " + username));
                
        if (!car.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        return ResponseEntity.ok(CarResponse.fromEntity(car));
    }

    /**
     * Erstellt oder aktualisiert das Fahrzeug des authentifizierten Users.
     * PUT: Upsert-Verhalten (Create wenn keines existiert, sonst Update).
     */
    @PutMapping("/profile/car")
    public ResponseEntity<CarResponse> createOrUpdateCar(@RequestBody CarRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


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
