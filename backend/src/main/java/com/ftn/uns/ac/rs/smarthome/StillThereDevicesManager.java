package com.ftn.uns.ac.rs.smarthome;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StillThereDevicesManager {
    private final Set<Integer> devices = ConcurrentHashMap.newKeySet();

    public StillThereDevicesManager() {
    }

    public void add(Integer deviceId) {
        devices.add(deviceId);
    }

    public boolean isntThere(Integer deviceId) {
        return !devices.contains(deviceId);
    }

    public void reset() {
        devices.clear();
    }
}
