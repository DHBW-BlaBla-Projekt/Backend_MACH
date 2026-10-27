import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;


@RestController
@RequestMapping("/api/vehicle-data")
@Validated
public class VehicleDataService {
    private final VehicleDataService vehicleDataService;

    public VehicleDataController(VehicleDataService vehicleDataService) {
        this.vehicleDataService = vehicleDataService;
    }

    @GetMapping("/makes")
    public ResponseEntity<List<VehicleMenuItemDto>> getMakes(
        @RequestParam
        @Min(value = 1984, message = "Das Baujahr muss mindestens 1984 sein.")
        @Max(value = 2030, message = "Das Baujahr ist zu weit in der Zukunft!")
        Integer year
    ) {
        return ResponseEntity.ok(vehicleDataService.getMakes(year))
    }

    @GetMapping("/models")
    public ResponseEntity<List<VehicleMenuItemDto>> getModels(
        @RequestParam
        @Min(1984)
        @Max(2030)
        Integer year,

        @RequestParam
        @NotBlank(message = "Die Marke darf nit leer sein.")
        @Size(max = 100, message = "Die Marke ist zu lang.")
        String make
    ) {
        return ResponseEntity.ok(vehicleDataService.getModels(year, make));
    }

    @GetMapping("/options")
    public ResponseEntity<List<VehicleOptionDto>> getOption(
        @RequestParam
        @Min(1984)
        @Max(2030)
        Integer year,

        @RequestParam
        @NotBlank
        @Size(max = 100)
        String make,

        @RequestParam
        @NotBlank
        @Size(max = 150)
        String model
    ) {
        return ResponseEntity.ok(vehicleDataService.getOptions(year, make, model))
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleDetailsDto getVehicleDetails(
        @PathVariable
        @Positive(message = "Die Vehicle ID muss positiv sein.")
        Long vehicleId
    ) {
        return ResponseEntity.ok(vehicleDataService.getVehicleDetails(vehicleId))
    }


}
