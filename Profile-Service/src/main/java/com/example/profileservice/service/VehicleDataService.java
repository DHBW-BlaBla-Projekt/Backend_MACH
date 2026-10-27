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

@Service
public class VehicleDataService {
    private static final int MIN_YEAR = 1984;
    private static final int MAX_YEAR = 2030;

    public List<VehicleMenuItemDto> getMakes(Integer year) {
        validateYear(year);

        // Erst dann Chache prüfen / API aufrufen
        return loadMakesFromCacheOrApi(year)
    }

    public List<VehicleMenuItemDto> getModels(Integer year, String make) {
        validateYear(year);
        String validatedMake = validateText(make, "Marke", 100);
        
        return loadModelsFromCacheOrApi(year, validatedMake)
    }

    public List<VehicleOptionDto> getOptions(
        Integer year,
        String make,
        String model) {

        validateYear(year);
        String validatedMake = validateText(make, "Marke", 100);
        String validatedModel = validateText(model, "Modell", 150);
            
        return loadOptionsFromCacheOrApi(year, validatedMake, validatedModel)
    }

    public VehicleDetailsDto getVehicleDetails(Long vehicleId) {
        if(vehicleId == null ||vehicleId < 0) {
            throw new IllegalArgumentException ("Die Vehicle ID muss eine positive Zahl sein!");
        }

        return loadVehicleDetailsFromCacheOrApi(vehicleId);
    }

    private void validateYear(Integer year) {
        if(year == null) {
            throw new IllegalArgumentException("Das Baujahr darf nicht leer sein!");
        }

        if(year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("Das Baujahr muss zwischen " + MIN_YEAR + " und " + MAX_YEAR + " liegen!");
        }
    }

    private String validateText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " darf nicht leer sein!");
        }

        String normalized = value.trim()

        if(normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " darf maximal " + maxLength + " Zeichen lang sein!");
        }
        return normalized;
    }

    private List<VehicleMenuItemDto> loadMakesFromCacheOrApi(Integer year) {
        throw new UnsupportedOperationException("Noch nicht implementiert!");
    }

    private List<VehicleMenuItemDto> loadModelsFromCacheOrApi(Integer year, String make) {
        throw new UnsupportedOperationException("Noch nicht implementiert!")
    }

    private List<VehicleMenuItemDto> loadOptionsFromCacheOrApi(Integer year, String make, String model) {
        throw new UnsupportedOperationException("Noch nicht implementiert!")
    }

    private List<VehicleMenuItemDto> loadVehicleDetailsFromCacheOrApi(Long vehicleId){
        throw new UnsupportedOperationException("Noch nicht implementiert!")
    }


}
