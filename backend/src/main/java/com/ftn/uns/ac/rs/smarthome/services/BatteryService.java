package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.devices.Battery;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.BatteryDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.BatteryRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.PropertyRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IBatteryService;
import com.ftn.uns.ac.rs.smarthome.utils.S3API;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class BatteryService extends GenericDeviceService<Battery, BatteryDTO> implements IBatteryService {
    private final BatteryRepository batteryRepository;

    public BatteryService(PropertyRepository propertyRepository,
                          DeviceRepository deviceRepository,
                          MessageSource messageSource,
                          S3API fileServerService,
                          BatteryRepository batteryRepository) throws IOException {
        super(propertyRepository, deviceRepository, messageSource, fileServerService);
        this.batteryRepository = batteryRepository;
    }

    @Override
    protected Battery createDevice(BatteryDTO dto, Property property) {
        return new Battery(property, dto.getName(), dto.getPowerSource(), dto.getEnergyConsumption(),
                dto.getCapacity());
    }

    @Override
    public List<Battery> getAllNonEmpty(int propertyId) {
        return batteryRepository.findAllByPropertyIdAndOccupiedCapacityGreaterThan(propertyId, 0.0);
    }

    @Override
    public List<Battery> getAllNonFull(int propertyId) {
        return batteryRepository.findAllNonFull(propertyId);
    }

    @Override
    public void update(Battery battery) {
        batteryRepository.save(battery);
    }
}
