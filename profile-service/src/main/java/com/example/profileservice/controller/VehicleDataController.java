package com.example.profileservice.controller;

import com.example.profileservice.dto.VehicleDetailsDto;
import com.example.profileservice.dto.VehicleMenuItemDto;
import com.example.profileservice.dto.VehicleOptionDto;
import com.example.profileservice.service.VehicleDataService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-data")
@Validated
public class VehicleDataController {

    private final VehicleDataService vehicleDataService;

    public VehicleDataController(VehicleDataService vehicleDataService) {
        this.vehicleDataService = vehicleDataService;
    }

    @GetMapping("/years")
    public ResponseEntity<List<VehicleMenuItemDto>> getYears() {
        return ResponseEntity.ok(vehicleDataService.getYears());
    }

    @GetMapping("/makes")
    public ResponseEntity<List<VehicleMenuItemDto>> getMakes(
            @RequestParam
            @Min(value = 1984, message = "Das Baujahr muss mindestens 1984 sein.")
            @Max(value = 2030, message = "Das Baujahr darf höchstens 2030 sein.")
            Integer year) {
        return ResponseEntity.ok(vehicleDataService.getMakes(year));
    }

    @GetMapping("/models")
    public ResponseEntity<List<VehicleMenuItemDto>> getModels(
            @RequestParam @Min(1984) @Max(2030) Integer year,
            @RequestParam
            @NotBlank(message = "Die Marke darf nicht leer sein.")
            @Size(max = 100, message = "Die Marke ist zu lang.")
            String make) {
        return ResponseEntity.ok(vehicleDataService.getModels(year, make));
    }

    @GetMapping("/options")
    public ResponseEntity<List<VehicleOptionDto>> getOptions(
            @RequestParam @Min(1984) @Max(2030) Integer year,
            @RequestParam @NotBlank @Size(max = 100) String make,
            @RequestParam @NotBlank @Size(max = 150) String model) {
        return ResponseEntity.ok(
                vehicleDataService.getOptions(year, make, model));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleDetailsDto> getVehicleDetails(
            @PathVariable
            @Positive(message = "Die Vehicle ID muss positiv sein.")
            Long vehicleId) {
        return ResponseEntity.ok(
                vehicleDataService.getVehicleDetails(vehicleId));
    }
}
