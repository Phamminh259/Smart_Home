package com.ftn.uns.ac.rs.smarthome;

import com.ftn.uns.ac.rs.smarthome.models.devices.Device;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StatusCheckerTask {
    private static final Logger log = LoggerFactory.getLogger(StatusCheckerTask.class);
    private final IDeviceService deviceService;
    private final StillThereDevicesManager stillThereDevicesManager;
    private static final int FIXED_RATE = 30000;
    public static final int INITIAL_DELAY = 4000;

    public StatusCheckerTask(IDeviceService deviceService,
                             StillThereDevicesManager stillThereDevicesManager) {
        this.deviceService = deviceService;
        this.stillThereDevicesManager = stillThereDevicesManager;
    }

    @Scheduled(initialDelay = INITIAL_DELAY, fixedRate = FIXED_RATE)
    public void runTask() {
        log.info("Status checker task started");
        List<Device> allDevices = deviceService.findAll();
        for (Device device : allDevices) {
            if (device.isStillThere()) {
                device.setOnline(true);
                device.setStillThere(false);
                deviceService.update(device);
            } else if (device.isOnline()) {
                log.info("Device {} is not responding", device.getName());
                device.setOnline(false);
                deviceService.update(device);
            }
        }
        stillThereDevicesManager.reset();
    }
}
