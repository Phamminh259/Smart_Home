package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.devices.Gate;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.GateDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.PropertyRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IGateService;
import com.ftn.uns.ac.rs.smarthome.utils.S3API;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GateService extends GenericDeviceService<Gate, GateDTO> implements IGateService {
    public GateService(PropertyRepository propertyRepository,
                       DeviceRepository deviceRepository,
                       MessageSource messageSource,
                       S3API fileServerService) throws IOException {
        super(propertyRepository, deviceRepository, messageSource, fileServerService);
    }

    @Override
    protected Gate createDevice(GateDTO dto, Property property) {
        return new Gate(dto, property);
    }
}
