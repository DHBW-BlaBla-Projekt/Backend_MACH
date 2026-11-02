package com.example.rideofferservice.graphql;

import com.example.rideofferservice.dto.RideResponse;
import com.example.rideofferservice.service.RideService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RideGraphQLResolver {

    private final RideService rideService;

    public RideGraphQLResolver(RideService rideService) {
        this.rideService = rideService;
    }

    @QueryMapping
    public List<RideResponse> availableRides() {
        return rideService.getAvailableRides();
    }
}
