package com.example.profileservice.service;

import com.example.profileservice.model.entity.Car;
import com.example.profileservice.repository.CarRepository;
import com.example.profileservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CarService {

    private static final Logger log = LoggerFactory.getLogger(CarService.class);

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public CarService(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
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
     * Erstellt oder aktualisiert das Fahrzeug (Upsert).
     */
    @Transactional
    public Car createOrUpdateCar(String username, String brand, String model,
                                 Double averageConsumptionLPer100km, Car.FuelType fuelType) {
        var user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden: " + username));

        Long userId = user.getId();

        if (carRepository.existsByUserId(userId)) {
            log.info("Fahrzeug für User {} existiert bereits, wird aktualisiert.", userId);
            return updateCar(userId, brand, model, averageConsumptionLPer100km, fuelType);
        } else {
            log.info("Neues Fahrzeug für User {} wird erstellt.", userId);
            return createCar(userId, brand, model, averageConsumptionLPer100km, fuelType);
        }
    }

    /**
     * Liefert das Hauptfahrzeug eines Users.
     */
    @Transactional(readOnly = true)
    public Optional<Car> getCarForUser(Long userId) {
        return carRepository.findByUserId(userId);
    }

    /**
     * Liefert das Hauptfahrzeug eines Users über den Username.
     */
    @Transactional(readOnly = true)
    public Optional<Car> getCarForUser(String username) {
        return userRepository.findByUsername(username.toLowerCase())
                .flatMap(user -> carRepository.findByUserId(user.getId()));
    }

    /**
     * Liefert ein Fahrzeug über seine ID (nützlich für den ride-offer-service).
     */
    @Transactional(readOnly = true)
    public Optional<Car> getCarById(Long carId) {
        return carRepository.findById(carId);
    }
}
