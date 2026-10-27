public class FuelEconomyClient {
    
    List<VehicleMenuItemDto> getYears();

    List<VehicleMenuItemDto> getMakes(int year);

    List<VehicleMenuItemDto> getModels(int year, String make);

    List<VehicleOptionDto> getOptions(int year, String make, String model);

    VehicleDetailsDto getVehicleDetails(long vehicleId);

    app.fueleconomy.base-url=https://www.fueleconomy.gov/ws/rest/vehicle
    app.fueleconomy.timeout-ms=5000
}
