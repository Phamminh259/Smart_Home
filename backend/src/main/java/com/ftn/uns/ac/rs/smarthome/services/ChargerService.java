package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.devices.Charger;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.ChargerDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.PropertyRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IChargerService;
import com.ftn.uns.ac.rs.smarthome.utils.S3API;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ChargerService extends GenericDeviceService<Charger, ChargerDTO> implements IChargerService {
    public ChargerService(PropertyRepository propertyRepository,
                          DeviceRepository deviceRepository,
                          MessageSource messageSource,
                          S3API fileServerService) throws IOException {
        super(propertyRepository, deviceRepository, messageSource, fileServerService);
    }

    @Override
    protected Charger createDevice(ChargerDTO dto, Property property) {
        return new Charger(dto, property);
    }
}
