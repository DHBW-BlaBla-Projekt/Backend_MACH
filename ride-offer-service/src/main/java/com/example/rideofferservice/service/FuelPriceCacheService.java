package com.example.rideofferservice.service;

import com.example.rideofferservice.client.TankKoenigClient;
import com.example.rideofferservice.entity.FuelType;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching-Layer für Kraftstoffpreise.
 * Holt Preise von TankKoenig-API und speichert sie mit TTL im Memory-Cache.
 * Fallback auf Hardcoded-Werte wenn API nicht erreichbar.
 */
@Service
public class FuelPriceCacheService {

    private static final Logger log = LoggerFactory.getLogger(FuelPriceCacheService.class);

    private final TankKoenigClient tankKoenigClient;

    @Value("${app.tankkoenig.price-cache-ttl-minutes:120}")
    private long cacheTtlMinutes;


    private volatile Map<FuelType, Double> cachedPrices;
    private volatile long lastFetchTime = 0;

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
     * Bei Cache-Miss oder TTL-Verbrauch wird die API aufgerufen.
     */
    public double getPrice(FuelType fuelType) {
        ensureCacheValid();
        
        Double price = cachedPrices.get(fuelType);
        if (price != null) {
            return price;
        }


        log.warn("No TankKoenig price for fuelType={}, using fallback value: {}", fuelType, FALLBACK_PRICES.get(fuelType));
        return FALLBACK_PRICES.getOrDefault(fuelType, 1.80);
    }

    /**
     * Prüft ob der Cache fresh ist oder ein Refetch nötig ist.
     */
    private synchronized void ensureCacheValid() {
        if (cachedPrices == null || isCacheExpired()) {
            fetchAndCachePrices();
        }
    }

    private boolean isCacheExpired() {
        return System.currentTimeMillis() - lastFetchTime > cacheTtlMinutes * 60 * 1000;
    }

    private synchronized void fetchAndCachePrices() {
        try {
            log.info("Fetching fuel prices from TankKoenig API...");
            Map<FuelType, Double> apiPrices = tankKoenigClient.fetchAveragePrices();
            cachedPrices = new ConcurrentHashMap<>(apiPrices);
            lastFetchTime = System.currentTimeMillis();
            log.info("Fuel prices updated: {}", cachedPrices);
        } catch (Exception e) {
            log.error("Failed to fetch fuel prices from TankKoenig API: {}. Using fallback.", e.getMessage());
            if (cachedPrices == null) {
                // Erster Aufruf schlägt fehl → Fallback initialisieren
                cachedPrices = new ConcurrentHashMap<>(FALLBACK_PRICES);
                lastFetchTime = System.currentTimeMillis();
            }
            // Bei späteren Fehlern bleibt der alte Cache erhalten
        }
    }

    /**
     * Wird für Tests / Hot-Reload benötigt.
     */
    public synchronized void refresh() {
        cachedPrices = null;
        lastFetchTime = 0;
        ensureCacheValid();
    }

    /**
     * Cache-TTL in Minuten (für Runtime-Konfiguration).
     */
    public long getCacheTtlMinutes() {
        return cacheTtlMinutes;
    }
}
