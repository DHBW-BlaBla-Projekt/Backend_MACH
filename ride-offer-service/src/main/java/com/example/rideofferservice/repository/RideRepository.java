package com.example.rideofferservice.repository;

import com.example.rideofferservice.entity.RideOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<RideOffer, Long> {

    List<RideOffer> findByDriverUsername(String username);

    List<RideOffer> findByDriverUsernameAndStatus(String username, RideOffer.RideStatus status);
    List<RideOffer> findByStatus(RideOffer.RideStatus status);

    List<RideOffer> findByStatusAndDriverUsername(RideOffer.RideStatus status, String username);

    /**
     * Finde Mitfahrten mit exaktem Start und Ziel.
     */
    @Query("SELECT r FROM RideOffer r WHERE r.startLocation = :startLocation " +
           "AND r.endLocation = :endLocation AND r.status = 'OFFERED'")
    List<RideOffer> findByStartAndEndLocation(@Param("startLocation") String startLocation,
                                              @Param("endLocation") String endLocation);

    /**
     * Finde Mitfahrten mit exaktem Start und Ziel innerhalb eines Zeitraum.
     */
    @Query("SELECT r FROM RideOffer r WHERE r.startLocation = :startLocation " +
           "AND r.endLocation = :endLocation AND r.status = 'OFFERED' " +
           "AND r.departureTime BETWEEN :from AND :to")
    List<RideOffer> findByStartAndEndBetween(@Param("startLocation") String startLocation,
                                             @Param("endLocation") String endLocation,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    /**
     * Finde Mitfahrten mit exaktem Start und Ziel ab einem Zeitpunkt.
     */
    @Query("SELECT r FROM RideOffer r WHERE r.startLocation = :startLocation " +
           "AND r.endLocation = :endLocation AND r.status = 'OFFERED' " +
           "AND r.departureTime >= :from")
    List<RideOffer> findByStartAndEndAfter(@Param("startLocation") String startLocation,
                                           @Param("endLocation") String endLocation,
                                           @Param("from") LocalDateTime from);

    /**
     * Finde Mitfahrten mit exaktem Start und Ziel bis zu einem Zeitpunkt.
     */
    @Query("SELECT r FROM RideOffer r WHERE r.startLocation = :startLocation " +
           "AND r.endLocation = :endLocation AND r.status = 'OFFERED' " +
           "AND r.departureTime <= :to")
    List<RideOffer> findByStartAndEndBefore(@Param("startLocation") String startLocation,
                                            @Param("endLocation") String endLocation,
                                            @Param("to") LocalDateTime to);

    /**
     * Finde eine einzelne Mitfahrt durch ihre ID.
     */
    Optional<RideOffer> findById(Long id);
}
