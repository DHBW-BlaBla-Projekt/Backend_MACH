package com.example.profileservice.dto;

// KI_LOG : 2
public class VehicleDetailsDto {
    private Long vehicleId;
    private Integer year;
    private String make;
    private String model;
    private Integer cityMpg;
    private Integer highwayMpg;
    private Integer combinedMpg;
    private Double consumptionLPer100Km;
    private String fuelType;

    public VehicleDetailsDto() {
    }

    public Long getVehicleId() {
        return vehicleId; }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId; }

    public Integer getYear()
    { return year; }

    public void setYear(Integer year) {
        this.year = year; }

    public String getMake() {
        return make; }

    public void setMake(String make) {
        this.make = make; }

    public String getModel() {
        return model; }

    public void setModel(String model) {
        this.model = model; }

    public Integer getCityMpg() {
        return cityMpg; }

    public void setCityMpg(Integer cityMpg) {
        this.cityMpg = cityMpg; }

    public Integer getHighwayMpg() {
        return highwayMpg; }

    public void setHighwayMpg(Integer highwayMpg) {
        this.highwayMpg = highwayMpg; }

    public Integer getCombinedMpg() {
        return combinedMpg; }

    public void setCombinedMpg(Integer combinedMpg) {
        this.combinedMpg = combinedMpg; }

    public Double getConsumptionLPer100Km() {
        return consumptionLPer100Km; }

    public void setConsumptionLPer100Km(Double consumptionLPer100Km) {
        this.consumptionLPer100Km = consumptionLPer100Km;
    }
    public String getFuelType() {
        return fuelType; }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType; }
}
