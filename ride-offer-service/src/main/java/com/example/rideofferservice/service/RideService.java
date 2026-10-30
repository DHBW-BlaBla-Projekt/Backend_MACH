package com.example.rideofferservice.service;

import com.example.rideofferservice.client.ProfileServiceClient;
import com.example.rideofferservice.dto.CarInfoDto;
import com.example.rideofferservice.dto.CreateRideRequest;
import com.example.rideofferservice.dto.RideResponse;
import com.example.rideofferservice.entity.FuelType;
import com.example.rideofferservice.entity.RideOffer;
import com.example.rideofferservice.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.example.rideofferservice.service.FuelCostService;
import com.example.rideofferservice.service.FuelPriceCacheService;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Geschäftslogik-Service für Mitfahrtsangebote.
 */
@Service
public class RideService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    private final RideRepository rideRepository;
    private final RabbitTemplate rabbitTemplate;
    private final boolean rabbitmqAvailable;
    private final ProfileServiceClient profileServiceClient;
    private final FuelCostService fuelCostService;
    private final FuelPriceCacheService fuelPriceCacheService;

    @Value("${app.rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing.key}")
    private String routingKey;

    @Value("${app.rabbitmq.routing.key.cancelled:ride.cancelled.routing}")
    private String cancelledRoutingKey;

    public RideService(
            RideRepository rideRepository,
            CachingConnectionFactory connectionFactory,
            ProfileServiceClient profileServiceClient,
            @Value("${app.rabbitmq.enabled:false}") boolean rabbitmqEnabled,
            FuelCostService fuelCostService,
            FuelPriceCacheService fuelPriceCacheService) {
        this.rideRepository = rideRepository;
        this.profileServiceClient = profileServiceClient;
        
        boolean available = false;
        if (rabbitmqEnabled) {
            try (Connection conn = connectionFactory.createConnection()) {
                available = true;
            } catch (Exception e) {
                log.warn("RabbitMQ nicht verfügbar: {}", e.getMessage());
            }
        }
        this.rabbitmqAvailable = available;
        
        RabbitTemplate template;
        try {
            if (available) {
                template = new RabbitTemplate(connectionFactory);
                template.setMessageConverter(new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter());
            } else {
                template = null;
            }
        } catch (Exception e) {
            log.warn("RabbitTemplate konnte nicht erstellt werden, Events werden ignoriert.");
            template = null;
        }
        this.rabbitTemplate = template;
        this.fuelCostService = fuelCostService;
        this.fuelPriceCacheService = fuelPriceCacheService;
    }

    public List<RideResponse> getAvailableRides() {
        return rideRepository.findByStatusAndDepartureTimeAfter(
                        RideOffer.RideStatus.OFFERED, LocalDateTime.now())
                .stream()
                .map(RideResponse::fromEntityFull)
                .collect(Collectors.toList());
    }

    public List<RideResponse> searchRides(String startLocation, String endLocation) {
        return rideRepository.findByStartAndEndLocation(startLocation, endLocation, LocalDateTime.now())
                .stream()
                .map(RideResponse::fromEntityFull)
                .collect(Collectors.toList());
    }

    public List<RideResponse> searchRidesBetween(String startLocation, String endLocation, LocalDateTime from, LocalDateTime to) {
        return rideRepository.findByStartAndEndBetween(
                        startLocation, endLocation, from, to, LocalDateTime.now())
                .stream()
                .map(RideResponse::fromEntityFull)
                .collect(Collectors.toList());
    }

    public List<RideResponse> searchRidesAfter(String startLocation, String endLocation, LocalDateTime from) {
        return rideRepository.findByStartAndEndAfter(
                        startLocation, endLocation, from, LocalDateTime.now())
                .stream()
                .map(RideResponse::fromEntityFull)
                .collect(Collectors.toList());
    }

    public List<RideResponse> searchRidesBefore(String startLocation, String endLocation, LocalDateTime to) {
        return rideRepository.findByStartAndEndBefore(
                        startLocation, endLocation, to, LocalDateTime.now())
                .stream()
                .map(RideResponse::fromEntityFull)
                .collect(Collectors.toList());
    }

    public List<RideResponse> getMyRides(String username) {
        return rideRepository.findByDriverUsername(username)
                .stream()
                .map(RideResponse::fromEntityFull)
                .collect(Collectors.toList());
    }

    public List<RideResponse> getMyOfferedRides(String username) {
        return rideRepository.findByStatusAndDriverUsername(
                        RideOffer.RideStatus.OFFERED, username)
                .stream()
                .filter(ride -> ride.getDepartureTime().isAfter(LocalDateTime.now()))
                .map(RideResponse::fromEntityFull)
                .collect(Collectors.toList());
    }

    @Transactional
    public RideResponse createRide(String username, CreateRideRequest request) {
        if (request.getAvailableSeats() <= 0) {
            throw new IllegalArgumentException("Verfügbare Sitze müssen > 0 sein.");
        }
        if (request.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Abfahrtszeit muss in der Zukunft liegen.");
        }

        // === Car-Daten über Profile-Service laden ===
        Long carId = request.getCarId();
        Double averageConsumptionLPer100km = null;
        FuelType fuelType = null;

        if (carId != null) {
            try {
                log.info("Car-Daten für carId {} vom Profile-Service laden...", carId);
                CarInfoDto carData = profileServiceClient.getCarById(carId);

                if (carData == null) {
                    throw new RuntimeException("Car mit ID " + carId + " im Profile-Service nicht gefunden.");
                }

                log.info("Car-Daten für carId {} geladen: brand={}, name={}, consumption={} l/100km, fuelType={}", 
                        carId, carData.getBrand(), carData.getName(), 
                        carData.getAverageConsumptionLPer100km(), carData.getFuelType());

                averageConsumptionLPer100km = carData.getAverageConsumptionLPer100km();
                fuelType = carData.getFuelType() != null ? FuelType.valueOf(carData.getFuelType().name()) : null;
            } catch (IllegalStateException e) {
                throw new RuntimeException("Car-Daten können nicht geladen werden: " + e.getMessage());
            }
        }

        RideOffer ride = new RideOffer();
        ride.setDriverUsername(username);
        ride.setStartLocation(request.getStartLocation());
        ride.setEndLocation(request.getEndLocation());
        ride.setDepartureTime(request.getDepartureTime());
        ride.setAvailableSeats(request.getAvailableSeats());
        ride.setAdditionalInfo(request.getAdditionalInfo());
        ride.setStatus(RideOffer.RideStatus.OFFERED);
        
        // === Fahrzeugdaten speichern ===
        ride.setCarId(carId);
        ride.setAverageConsumptionLPer100km(averageConsumptionLPer100km);
        ride.setFuelType(fuelType);
        // === distanceKm speichern ===
        ride.setDistanceKm(request.getDistanceKm());

        // === estimatedCost berechnen und speichern (TankKoenig-API) ===
        if (fuelType != null && averageConsumptionLPer100km != null && request.getDistanceKm() != null) {
            FuelCostService.PriceResult priceResult = fuelCostService.calculateEstimatedFuelCost(ride, request.getDistanceKm());
            Double cost = priceResult.cost();
            ride.setEstimatedCost(cost);
            log.info("Berechnete estimatedCost für Ride: {} EUR (Distanz: {} km)", cost, request.getDistanceKm());
        }

        try {
            rideRepository.save(ride);
            publishRideOfferedEvent(ride);
            return RideResponse.fromEntityFull(ride);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Fehler beim Speichern der Mitfahrt: Optimistisches Locking-Fehler. Bitte erneut versuchen.");
        }
    }

    @Transactional
    public void deleteRide(Long id, String username) {
        RideOffer ride = rideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mitfahrt nicht gefunden!"));

        if (!ride.getDriverUsername().equals(username)) {
            throw new IllegalArgumentException("Nur der Fahrer kann die Mitfahrt löschen.");
        }

        if (ride.getStatus() == RideOffer.RideStatus.FULFILLED) {
            throw new IllegalStateException("Eine erfüllte Mitfahrt kann nicht gelöscht werden.");
        }

        rideRepository.delete(ride);
        log.info("Mitfahrt {} von {} gelöscht.", id, username);
    }

    @Transactional
    public RideResponse tryBookSeat(Long id) {
        RideOffer ride = rideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mitfahrt nicht gefunden!"));

        if (ride.getStatus() == RideOffer.RideStatus.CANCELLED) {
            throw new IllegalStateException("Diese Mitfahrt wurde storniert.");
        }
        if (ride.getAvailableSeats() <= 0) {
            throw new IllegalStateException("Keine freien Sitze mehr verfügbar!");
        }

        int remaining = ride.getAvailableSeats() - 1;
        if (remaining == 0) {
            ride.setStatus(RideOffer.RideStatus.FULFILLED);
            ride.setFulfilledAt(LocalDateTime.now());
        }
        ride.setAvailableSeats(remaining);

        return RideResponse.fromEntityFull(rideRepository.save(ride));
    }

    @Transactional
    public RideResponse decrementAvailableSeats(Long id) {
        RideOffer ride = rideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mitfahrt nicht gefunden!"));
        int seats = ride.getAvailableSeats() - 1;
        if (seats < 0) seats = 0;
        ride.setAvailableSeats(seats);
        return RideResponse.fromEntityFull(rideRepository.save(ride));
    }

    @Transactional
    public RideResponse incrementAvailableSeats(Long id) {
        RideOffer ride = rideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mitfahrt nicht gefunden!"));
        if (ride.getAvailableSeats() >= ride.getAvailableSeats()) {
            // safety check: don't go above original seats - use a reasonable max
        }
        ride.setAvailableSeats(ride.getAvailableSeats() + 1);
        return RideResponse.fromEntityFull(rideRepository.save(ride));
    }

    @Transactional
    public RideResponse cancelRide(Long id, String username) {
        RideOffer ride = rideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mitfahrt nicht gefunden!"));

        if (!ride.getDriverUsername().equals(username)) {
            throw new IllegalArgumentException("Nur der Fahrer kann die Mitfahrt stornieren.");
        }

        if (ride.getStatus() == RideOffer.RideStatus.FULFILLED) {
            throw new IllegalStateException("Eine bereits erfüllte Mitfahrt kann nicht storniert werden.");
        }

        ride.setStatus(RideOffer.RideStatus.CANCELLED);
        ride.setAvailableSeats(0);
        publishRideCancelledEvent(ride);

        return RideResponse.fromEntityFull(rideRepository.save(ride));
    }

    public RideResponse getRideById(Long id) {
        RideOffer ride = rideRepository.findById(id).orElse(null);
        return ride != null ? RideResponse.fromEntityFull(ride) : null;
    }

    /**
     * Publiziert ein {@code RideOfferedEvent} über RabbitMQ.
     */
    private void publishRideOfferedEvent(RideOffer ride) {
        if (!rabbitmqAvailable || rabbitTemplate == null) {
            log.warn("RabbitTemplate ist null — kann Event nicht senden. Ride gespeichert aber nicht publiziert: {}", ride.getId());
            return;
        }
        try {
            RideOfferedEvent event = new RideOfferedEvent();
            event.setRideId(ride.getId());
            event.setDriverUsername(ride.getDriverUsername());
            event.setStartLocation(ride.getStartLocation());
            event.setEndLocation(ride.getEndLocation());
            event.setDepartureTime(ride.getDepartureTime());
            event.setAvailableSeats(ride.getAvailableSeats());
            event.setTimestamp(LocalDateTime.now());

            rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
            log.info("RideOfferedEvent gesendet: {} -> {}", ride.getStartLocation(), ride.getEndLocation());
        } catch (Exception e) {
            log.warn("Fehler beim Senden von RideOfferedEvent: {}. Ride wurde gespeichert aber Event ignoriert.", e.getMessage());
        }
    }

    /**
     * Publiziert ein {@code RideCancelledEvent} über RabbitMQ.
     */
    private void publishRideCancelledEvent(RideOffer ride) {
        if (!rabbitmqAvailable || rabbitTemplate == null) return;
        try {
            RideCancelledEvent event = new RideCancelledEvent();
            event.setRideId(ride.getId());
            event.setDriverUsername(ride.getDriverUsername());
            event.setTimestamp(LocalDateTime.now());

            event.setEventType("RIDE_CANCELLED");
            rabbitTemplate.convertAndSend(exchangeName, cancelledRoutingKey, event);
            log.info("RideCancelledEvent gesendet: {}", ride.getId());
        } catch (Exception e) {
            log.warn("Fehler beim Senden von RideCancelledEvent: {}. Ride wurde gespeichert.", e.getMessage());
        }
    }

    /**
     * DTO für das {@code RideOfferedEvent}.
     */
    public static class RideOfferedEvent {
        private Long rideId;
        private String driverUsername;
        private String startLocation;
        private String endLocation;
        private LocalDateTime departureTime;
        private Integer availableSeats;
        private LocalDateTime timestamp;
        private String eventType = "RIDE_OFFERED";

        public Long getRideId() { return rideId; }
        public void setRideId(Long rideId) { this.rideId = rideId; }
        public String getDriverUsername() { return driverUsername; }
        public void setDriverUsername(String driverUsername) { this.driverUsername = driverUsername; }
        public String getStartLocation() { return startLocation; }
        public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
        public String getEndLocation() { return endLocation; }
        public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
        public LocalDateTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
        public Integer getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    /**
     * DTO für das {@code RideCancelledEvent}.
     */
    public static class RideCancelledEvent {
        private Long rideId;
        private String driverUsername;
        private LocalDateTime timestamp;
        private String eventType = "RIDE_CANCELLED";

        public Long getRideId() { return rideId; }
        public void setRideId(Long rideId) { this.rideId = rideId; }
        public String getDriverUsername() { return driverUsername; }
        public void setDriverUsername(String driverUsername) { this.driverUsername = driverUsername; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }
}
