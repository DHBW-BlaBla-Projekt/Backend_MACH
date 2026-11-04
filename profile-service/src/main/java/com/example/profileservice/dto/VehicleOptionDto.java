package com.example.profileservice.dto;

// KI_LOG : 2
public class VehicleOptionDto {
    private Long vehicleId;
    private String label;

    public VehicleOptionDto() {
    }

    public VehicleOptionDto(Long vehicleId, String label) {
        this.vehicleId = vehicleId;
        this.label = label;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
