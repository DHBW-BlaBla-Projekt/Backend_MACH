package com.example.authservice.service;

import com.example.authservice.model.entity.Car;
import com.example.authservice.repository.CarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CarService {

    private static final Logger log = LoggerFactory.getLogger(CarService.class);

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * Erstellt ein neues Hauptfahrzeug für den User.
     */
    @Transactional
    public Car createCar(Long userId, String brand, String model,
                         Double averageConsumptionLPer100km, Car.FuelType fuelType) {
        if (carRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Der Benutzer hat bereits ein Fahrzeug hinterlegt. Bitte aktualisieren Sie dieses.");
        }

        Car car = new Car(userId, brand, model, averageConsumptionLPer100km, fuelType);
        Car savedCar = carRepository.save(car);
        log.info("Neues Fahrzeug für User {} erstellt: {} {}", userId, brand, model);
        return savedCar;
    }

    /**
     * Aktualisiert das Hauptfahrzeug des Users.
     */
    @Transactional
    public Car updateCar(Long userId, String brand, String model,
                         Double averageConsumptionLPer100km, Car.FuelType fuelType) {
        Car existing = carRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Kein Fahrzeug für User " + userId + " gefunden."));

        existing.setBrand(brand);
        existing.setModel(model);
        existing.setAverageConsumptionLPer100km(averageConsumptionLPer100km);
        existing.setFuelType(fuelType);

        Car updatedCar = carRepository.save(existing);
        log.info("Fahrzeug für User {} aktualisiert: {} {}", userId, brand, model);
        return updatedCar;
    }

    /**
     * Liefert das Hauptfahrzeug eines Users.
     */
    @Transactional(readOnly = true)
    public Optional<Car> getCarForUser(Long userId) {
        return carRepository.findByUserId(userId);
    }

    /**
     * Liefert ein Fahrzeug über seine ID (nützlich für den ride-offer-service).
     */
    @Transactional(readOnly = true)
    public Optional<Car> getCarById(Long carId) {
        return carRepository.findById(carId);
    }
}