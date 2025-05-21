package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.ACStateChange;
import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.devices.SolarPanelSystem;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.SolarPanelSystemDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.PropertyRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.ISolarPanelSystemService;
import com.ftn.uns.ac.rs.smarthome.utils.S3API;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class SolarPanelSystemService extends GenericDeviceService<SolarPanelSystem, SolarPanelSystemDTO> implements ISolarPanelSystemService {
    private final MqttService mqttService;
    private final InfluxService influxService;

    public SolarPanelSystemService(PropertyRepository propertyRepository,
                                   DeviceRepository deviceRepository,
                                   MessageSource messageSource,
                                   S3API fileServerService,
                                   MqttService mqttService,
                                   InfluxService influxService) throws IOException {
        super(propertyRepository, deviceRepository, messageSource, fileServerService);
        this.mqttService = mqttService;
        this.influxService = influxService;
    }

    @Override
    protected SolarPanelSystem createDevice(SolarPanelSystemDTO dto, Property property) {
        return new SolarPanelSystem(dto, property);
    }

    @Override
    public void turnOffSolarPanelSystem(Integer id, Integer userId) {
        SolarPanelSystem solarPanelSystem = (SolarPanelSystem) deviceRepository.findById(id).orElse(null);
        if (solarPanelSystem != null) {
            if (!solarPanelSystem.getIsOn()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("device.already.off", null, Locale.getDefault()));
            }
            solarPanelSystem.setIsOn(false);
            deviceRepository.save(solarPanelSystem);

            try {
                mqttService.publishMessageToTopic("OFF", "commands/sps/" + solarPanelSystem.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String,String> map = new HashMap<>();
            map.put("userId", String.valueOf(userId));
            map.put("deviceId", String.valueOf(id));

            influxService.save("states", "OFF", new Date(), map);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("device.not.found", null, Locale.getDefault()));
        }
    }

    @Override
    public void turnOnSolarPanelSystem(Integer id, Integer userId) {
        SolarPanelSystem solarPanelSystem = (SolarPanelSystem) deviceRepository.findById(id).orElse(null);
        if (solarPanelSystem != null) {
            if (solarPanelSystem.getIsOn()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("device.already.on", null, Locale.getDefault()));
            }
            solarPanelSystem.setIsOn(true);
            deviceRepository.save(solarPanelSystem);

            try {
                mqttService.publishMessageToTopic("ON", "commands/sps/" + solarPanelSystem.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String,String> map = new HashMap<>();
            map.put("userId", String.valueOf(userId));
            map.put("deviceId", String.valueOf(id));

            influxService.save("states", "ON", new Date(), map);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("device.not.found", null, Locale.getDefault()));
        }
    }
}
