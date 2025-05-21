package com.ftn.uns.ac.rs.smarthomesockets.services;

import com.ftn.uns.ac.rs.smarthomesockets.models.devices.AirConditioner;
import com.ftn.uns.ac.rs.smarthomesockets.models.devices.Device;
import com.ftn.uns.ac.rs.smarthomesockets.models.devices.WashingMachine;
import com.ftn.uns.ac.rs.smarthomesockets.models.dtos.DeviceCapabilities;
import com.ftn.uns.ac.rs.smarthomesockets.models.enums.TemperatureUnit;
import com.ftn.uns.ac.rs.smarthomesockets.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
//thông tin khả năng hoạt động của một thiết bị
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceCapabilities getDeviceCapabilities(Integer deviceId) throws Exception {
        Optional<Device> device = this.deviceRepository.findById(deviceId);
        Map<String,String> capabilities = new HashMap<>();
        if(device.isEmpty()) {
            throw new Exception("Device does not exist!");
        }
        else {
            if(device.get().getClass().equals(AirConditioner.class)){
                AirConditioner ac = (AirConditioner)device.get();
                capabilities.put("temperatureUnit", ac.getTemperatureUnit() == TemperatureUnit.CELSIUS? "C" : "F");
                capabilities.put("maxTemperature", ac.getMaxTemperature().toString());
                capabilities.put("minTemperature", ac.getMinTemperature().toString());
                capabilities.put("fanSpeed",ac.getFanSpeed().toString());
                capabilities.put("cooling",ac.getCooling().toString());
                capabilities.put("heating", ac.getHeating().toString());
                capabilities.put("dry", ac.getDry().toString());
                capabilities.put("auto", ac.getAuto().toString());
                capabilities.put("health", ac.getHealth().toString());
                capabilities.put("fungusPrevention", ac.getFungusPrevention().toString());
            }
            if(device.get().getClass().equals(WashingMachine.class)){
                WashingMachine wm = (WashingMachine)device.get();
                capabilities.put("temperatureUnit", wm.getTemperatureUnit() == TemperatureUnit.CELSIUS? "C" : "F");
                capabilities.put("maxTemperature", wm.getTemperatureMax().toString());
                capabilities.put("minTemperature", wm.getTemperatureMin().toString());
                capabilities.put("minCentrifuge",wm.getCentrifugeMin().toString());
                capabilities.put("maxCentrifuge",wm.getCentrifugeMax().toString());
                capabilities.put("cottons", wm.getCottons().toString());
                capabilities.put("synthetics", wm.getSynthetics().toString());
                capabilities.put("daily_express", wm.getDailyExpress().toString());
                capabilities.put("wool", wm.getWool().toString());
                capabilities.put("dark_wash", wm.getDarkWash().toString());
                capabilities.put("outdoor", wm.getOutdoor().toString());
                capabilities.put("shirts", wm.getShirts().toString());
                capabilities.put("duvet", wm.getDuvet().toString());
                capabilities.put("mixed", wm.getMixed().toString());
                capabilities.put("steam", wm.getSteam().toString());
                capabilities.put("rinse_spin", wm.getRinseAndSpin().toString());
                capabilities.put("spin_only", wm.getSpinOnly().toString());
                capabilities.put("hygiene", wm.getHygiene().toString());
            }
        }
        return new DeviceCapabilities(capabilities);
    }
}
