package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.devices.WashingMachine;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.WashingMachineDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.PropertyRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IWashingMachineService;
import com.ftn.uns.ac.rs.smarthome.utils.S3API;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WashingMachineService extends GenericDeviceService<WashingMachine, WashingMachineDTO> implements IWashingMachineService {

    public WashingMachineService(PropertyRepository propertyRepository,
                                 DeviceRepository deviceRepository,
                                 MessageSource messageSource,
                                 S3API fileServerService) throws IOException {
        super(propertyRepository, deviceRepository, messageSource, fileServerService);
    }

    @Override
    protected WashingMachine createDevice(WashingMachineDTO dto, Property property) {
        return new WashingMachine(dto, property);
    }
}
