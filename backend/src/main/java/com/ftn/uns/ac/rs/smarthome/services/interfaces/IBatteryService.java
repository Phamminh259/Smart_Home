package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.devices.Battery;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.BatteryDTO;

import java.io.IOException;
import java.util.List;

public interface IBatteryService {
    void register(BatteryDTO batteryDTO, User user) throws IOException;

    List<Battery> getAllNonEmpty(int propertyId);

    List<Battery> getAllNonFull(int propertyId);

    void update(Battery battery);
}

