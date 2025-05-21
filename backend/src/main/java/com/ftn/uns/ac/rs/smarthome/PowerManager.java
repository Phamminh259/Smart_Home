package com.ftn.uns.ac.rs.smarthome;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class PowerManager {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(PowerManager.class);
    private final ConcurrentHashMap<Integer, Double> powerConsumption = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Double> powerProduction = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Double> powerBalance = new ConcurrentHashMap<>();

    public PowerManager() {
    }

    public void addConsumption(int propertyId, double consumption) {
        powerConsumption.put(propertyId, getPowerConsumption(propertyId) + consumption);
        powerBalance.put(propertyId, getPowerProduction(propertyId) - getPowerConsumption(propertyId));
        logger.info("Consumption: " + powerConsumption + " Production: " + powerProduction + " Balance: " + powerBalance);
    }

    public void addProduction(int propertyId, double production) {
        powerProduction.put(propertyId, getPowerProduction(propertyId) + production);
        powerBalance.put(propertyId, getPowerProduction(propertyId) - getPowerConsumption(propertyId));
        logger.info("Consumption: " + powerConsumption + " Production: " + powerProduction + " Balance: " + powerBalance);
    }

    public double getPowerBalance(int propertyId) {
        return powerBalance.getOrDefault(propertyId, 0.0);
    }

    public double getPowerConsumption(int propertyId) {
        return powerConsumption.getOrDefault(propertyId, 0.0);
    }

    public double getPowerProduction(int propertyId) {
        return powerProduction.getOrDefault(propertyId, 0.0);
    }

    public void reset() {
        powerConsumption.clear();
        powerProduction.clear();
        powerBalance.clear();
        logger.info("Consumption: " + powerConsumption + " Production: " + powerProduction + " Balance: " + powerBalance);
    }
}
