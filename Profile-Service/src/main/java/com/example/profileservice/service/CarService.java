package com.example.profileservice.service;

import com.example.profileservice.model.entity.Car;
import com.example.profileservice.repository.CarRepository;
import com.example.profileservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
     * Erstellt ein neues Fahrzeug für den User.
     */
    @Transactional
    public Car createCar(Long userId, String name, String brand, String model, String licensePlate,
                         Double averageConsumptionLPer100km, Car.FuelType fuelType) {
        Car car = new Car();
        car.setUserId(userId);
        car.setName(name != null ? name : (brand + " " + model));
        car.setBrand(brand);
        car.setModel(model);
        car.setLicensePlate(licensePlate);
        car.setAverageConsumptionLPer100km(averageConsumptionLPer100km);
        car.setFuelType(fuelType);

        Car savedCar = carRepository.save(car);
        log.info("Neues Fahrzeug {} für User {} erstellt.", savedCar.getId(), userId);
        return savedCar;
    }

    /**
     * Aktualisiert ein bestehendes Fahrzeug.
     */
    @Transactional
    public Car updateCar(Long carId, Long userId, String name, String brand, String model, String licensePlate,
                         Double averageConsumptionLPer100km, Car.FuelType fuelType) {
        Car existing = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug nicht gefunden: " + carId));

        if (!existing.getUserId().equals(userId)) {
            throw new IllegalStateException("Du darfst nur deine eigenen Fahrzeuge bearbeiten.");
        }

        if (name != null) existing.setName(name);
        if (brand != null) existing.setBrand(brand);
        if (model != null) existing.setModel(model);
        if (licensePlate != null) existing.setLicensePlate(licensePlate);
        if (averageConsumptionLPer100km != null) existing.setAverageConsumptionLPer100km(averageConsumptionLPer100km);
        if (fuelType != null) existing.setFuelType(fuelType);

        Car updatedCar = carRepository.save(existing);
        log.info("Fahrzeug {} für User {} aktualisiert.", carId, userId);
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
            return updateCarForExisting(userId, brand, model, averageConsumptionLPer100km, fuelType);
        } else {
            log.info("Neues Fahrzeug für User {} wird erstellt.", userId);
            return createCar(userId, brand + " " + model, brand, model, null, averageConsumptionLPer100km, fuelType);
        }
    }

    private Car updateCarForExisting(Long userId, String brand, String model, Double avg, Car.FuelType fuelType) {
        // Da wir jetzt mehrere Autos haben können, suchen wir das erste für diesen User oder erstellen eins neu
        // Wir nutzen hier einfach eine neue Methode, die auf die alte Logik (Upsert) zurückgreift 
        // oder einfach ein neues erstellt. Für die Migration zu mehreren Autos: immer createCar aufrufen?
        // Der Controller nutzt diese Methode aktuell für den alten "ein Auto"-Fall. 
        // Wir ändern das Verhalten hier: Wenn es bereits existiert, updaten wir es (wie bisher).
        List<Car> cars = carRepository.findByUserId(userId);
        if (!cars.isEmpty()) {
            Car existing = cars.get(0);
            existing.setBrand(brand);
            existing.setModel(model);
            existing.setAverageConsumptionLPer100km(avg);
            existing.setFuelType(fuelType);
            return carRepository.save(existing);
        } else {
            return createCar(userId, brand + " " + model, brand, model, null, avg, fuelType);
        }
    }

    /**
     * Liefert alle Fahrzeuge eines Users.
     */
    @Transactional(readOnly = true)
    public List<Car> getAllCarsForUser(Long userId) {
        return carRepository.findByUserId(userId);
    }

    /**
     * Liefert alle Fahrzeuge eines Users über den Username.
     */
    @Transactional(readOnly = true)
    public List<Car> getAllCarsByUsername(String username) {
        var user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden: " + username));
        return carRepository.findByUserId(user.getId());
    }

    /**
     * Liefert alle Fahrzeuge eines Users über den Username (für Legacy).
     */
    @Transactional(readOnly = true)
    public Optional<Car> getCarForUser(String username) {
        var user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden: " + username));
        List<Car> cars = carRepository.findByUserId(user.getId());
        return cars.isEmpty() ? Optional.empty() : Optional.of(cars.get(0));
    }

    /**
     * Liefert alle Fahrzeuge eines Users über die userId (für Legacy).
     */
    @Transactional(readOnly = true)
    public Optional<Car> getCarForUser(Long userId) {
        List<Car> cars = carRepository.findByUserId(userId);
        return cars.isEmpty() ? Optional.empty() : Optional.of(cars.get(0));
    }

    /**
     * Liefert ein Fahrzeug über seine ID.
     */
    @Transactional(readOnly = true)
    public Optional<Car> getCarById(Long carId) {
        return carRepository.findById(carId);
    }
}
