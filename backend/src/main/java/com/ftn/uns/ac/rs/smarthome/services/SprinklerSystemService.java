package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.devices.SprinklerSystem;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.SprinklerSystemDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.PropertyRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.ISprinklerSystemService;
import com.ftn.uns.ac.rs.smarthome.utils.S3API;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SprinklerSystemService extends GenericDeviceService<SprinklerSystem, SprinklerSystemDTO> implements ISprinklerSystemService {
    public SprinklerSystemService(PropertyRepository propertyRepository,
                                  DeviceRepository deviceRepository,
                                  MessageSource messageSource,
                                  S3API fileServerService) throws IOException {
        super(propertyRepository, deviceRepository, messageSource, fileServerService);
    }

    @Override
    protected SprinklerSystem createDevice(SprinklerSystemDTO dto, Property property) {
        return new SprinklerSystem(dto, property);
    }
}
