package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.TemperatureUnit;
import com.ftn.uns.ac.rs.smarthome.models.devices.Device;
import com.ftn.uns.ac.rs.smarthome.models.devices.Thermometer;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.ThermometerDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.PropertyRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IThermometerService;
import com.ftn.uns.ac.rs.smarthome.utils.S3API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

@Service
public class ThermometerService extends GenericDeviceService<Thermometer, ThermometerDTO> implements IThermometerService {
    private static final Logger log = LoggerFactory.getLogger(ThermometerService.class);
    private final MqttService mqttService;

    public ThermometerService(PropertyRepository propertyRepository,
                              DeviceRepository deviceRepository,
                              MessageSource messageSource,
                              S3API fileServerService,
                              MqttService mqttService) throws IOException {
        super(propertyRepository, deviceRepository, messageSource, fileServerService);
        this.mqttService = mqttService;
    }

    @Override
    protected Thermometer createDevice(ThermometerDTO dto, Property property) {
        return new Thermometer(property, dto.getName(), dto.getPowerSource(), dto.getEnergyConsumption(),
                dto.getTemperatureUnit());
    }

    @Override
    public void changeThermometerTempUnit(Integer id, TemperatureUnit unit) {
        Optional<Device> device = deviceRepository.findById(id);
        if (device.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("device.notFound", null, Locale.getDefault()));
        }
        if (!(device.get() instanceof Thermometer thermometer)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("device.notThermometer", null, Locale.getDefault()));
        }
        thermometer.setTemperatureUnit(unit);
        deviceRepository.save(thermometer);
        try {
            mqttService.publishCommandMessage(String.valueOf(id));
        } catch (MqttException e) {
            log.error("Error while publishing command message to topic: {}", e.getMessage());
        }
    }
}
