package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.devices.Lamp;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.DeviceDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.PropertyRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.ILampService;
import com.ftn.uns.ac.rs.smarthome.utils.S3API;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LampService extends GenericDeviceService<Lamp, DeviceDTO> implements ILampService {
    public LampService(PropertyRepository propertyRepository,
                       DeviceRepository deviceRepository,
                       MessageSource messageSource,
                       S3API fileServerService) throws IOException {
        super(propertyRepository, deviceRepository, messageSource, fileServerService);
    }

    @Override
    protected Lamp createDevice(DeviceDTO dto, Property property) {
        return new Lamp(dto, property);
    }
}
