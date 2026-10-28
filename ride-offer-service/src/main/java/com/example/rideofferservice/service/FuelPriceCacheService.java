package com.example.rideofferservice.service;

import com.example.rideofferservice.client.TankKoenigClient;
import com.example.rideofferservice.entity.FuelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FuelPriceCacheService {

    private static final Logger log = LoggerFactory.getLogger(FuelPriceCacheService.class);

    private final TankKoenigClient tankKoenigClient;

    @Value("${app.tankkoenig.price-cache-ttl-minutes:120}")
    private long cacheTtlMinutes = 120;

    private volatile Map<FuelType, Double> cachedPrices = new ConcurrentHashMap<>();
    private volatile Map<FuelType, Boolean> priceFromApiMap = new ConcurrentHashMap<>();
    private volatile long lastFetchTime = 0;
    private volatile boolean apiAvailable = false;

    // Hardcoded Fallback-Preise (EUR pro Liter)
    private static final Map<FuelType, Double> FALLBACK_PRICES = Map.of(
            FuelType.PETROL, 1.85,
            FuelType.DIESEL, 1.70,
            FuelType.ELECTRIC, 0.45,
            FuelType.HYBRID, 1.75
    );

    public FuelPriceCacheService(TankKoenigClient tankKoenigClient) {
        this.tankKoenigClient = tankKoenigClient;
    }

    /**
     * Liefert den aktuellen Preis pro Liter für eine Kraftstoffart.
     */
    public double getPrice(FuelType fuelType) {
        if (fuelType == null) {
            return 0.0;
        }
        ensureCacheValid();
        Double price = cachedPrices.get(fuelType);
        if (price != null) {
            return price;
        }
        log.warn("No TankKoenig price for fuelType={}, using fallback value: {}", fuelType, FALLBACK_PRICES.get(fuelType));
        return FALLBACK_PRICES.getOrDefault(fuelType, 1.80);
    }

    /**
     * Gibt zurück, ob der Preis für den given FuelType von der TankKoenig-API stammt.
     */
    public boolean isPriceFromApi(FuelType fuelType) {
        if (fuelType == null) {
            return false;
        }
        ensureCacheValid();
        return Boolean.TRUE.equals(priceFromApiMap.get(fuelType));
    }

    /**
     * Gibt zurück, ob die API generell verfügbar ist (mindestens eine Kraftstoffart erfolgreich).
     */
    public boolean isApiAvailable() {
        return apiAvailable;
    }

    private synchronized void ensureCacheValid() {
        if (cachedPrices == null || cachedPrices.isEmpty() || isCacheExpired()) {
            fetchAndCachePrices();
        }
    }

    private boolean isCacheExpired() {
        return System.currentTimeMillis() - lastFetchTime > cacheTtlMinutes * 60 * 1000;
    }

    private synchronized void fetchAndCachePrices() {
        try {
            log.info("Fetching fuel prices from TankKoenig API...");
            Map<FuelType, Double> rawPrices = tankKoenigClient.fetchAveragePrices();
            
            // Filter out null values — ConcurrentHashMap does not accept null keys or values
            Map<FuelType, Double> apiPrices = new ConcurrentHashMap<>();
            for (Map.Entry<FuelType, Double> entry : rawPrices.entrySet()) {
                if (entry.getValue() != null) {
                    apiPrices.put(entry.getKey(), entry.getValue());
                }
            }
            // Speichere die gefilterten Preise im Cache
            cachedPrices = new ConcurrentHashMap<>(apiPrices);
            
            priceFromApiMap = new ConcurrentHashMap<>();
            for (FuelType ft : FuelType.values()) {
                priceFromApiMap.put(ft, apiPrices.containsKey(ft));
            }
            lastFetchTime = System.currentTimeMillis();
            apiAvailable = true;
            log.info("Fuel prices updated: {}, from API: {}", cachedPrices, priceFromApiMap);
        } catch (Exception e) {
            log.error("Failed to fetch fuel prices from TankKoenig API: {}. Using fallback.", e.getMessage());
            apiAvailable = false;
            
            if (cachedPrices == null || cachedPrices.isEmpty()) {
                cachedPrices = new ConcurrentHashMap<>(FALLBACK_PRICES);
                priceFromApiMap = new ConcurrentHashMap<>();
                for (FuelType ft : FuelType.values()) {
                    priceFromApiMap.put(ft, false);  // alle Fallback
                }
            }
            // Avoid retrying the unavailable upstream on every request.
            lastFetchTime = System.currentTimeMillis();
        }
    }

    public synchronized void refresh() {
        cachedPrices = null;
        priceFromApiMap = new ConcurrentHashMap<>();
        lastFetchTime = 0;
        ensureCacheValid();
    }

    public Map<FuelType, Double> getCachedPrices() {
        ensureCacheValid();
        return Map.copyOf(cachedPrices);
    }

    public long getCacheTtlMinutes() {
        return cacheTtlMinutes;
    }
}
