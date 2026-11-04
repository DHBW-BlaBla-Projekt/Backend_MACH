package com.example.rideofferservice.graphql;

import com.example.rideofferservice.dto.RideResponse;
import com.example.rideofferservice.entity.RideOffer;
import com.example.rideofferservice.service.RideService;
import org.springframework.beans.BeanUtils;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RideGraphQLResolver {

    private final RideService rideService;

    public RideGraphQLResolver(RideService rideService) {
        this.rideService = rideService;
    }

    @QueryMapping
    public List<RideOffer> availableRides() {
        return rideService.getAvailableRides().stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }

    private RideOffer toEntity(RideResponse response) {
        RideOffer entity = new RideOffer();
        BeanUtils.copyProperties(response, entity, "status", "fuelType", "averageConsumptionLPer100km");
        if (response.getStatus() != null) {
            entity.setStatus(
                switch (response.getStatus()) {
                    case OFFERED -> RideOffer.RideStatus.OFFERED;
                    case FULFILLED -> RideOffer.RideStatus.FULFILLED;
                    case CANCELLED -> RideOffer.RideStatus.CANCELLED;
                    default -> RideOffer.RideStatus.OFFERED;
                }
            );
        } else {
            entity.setStatus(RideOffer.RideStatus.OFFERED);
        }
        return entity;
    }
}
