package com.ftn.uns.ac.rs.smarthomesimulator;

import com.ftn.uns.ac.rs.smarthomesimulator.models.devices.Device;
import com.ftn.uns.ac.rs.smarthomesimulator.services.interfaces.IDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
//Tự động phát hiện và khởi động thiết bị nếu bị tắt hoặc chưa khởi động.
@Component
public class DeviceDetectionTask {
    private static final Logger log = LoggerFactory.getLogger(DeviceDetectionTask.class);
    private static final int FIXED_RATE = 15000;//nhiệm vụ sẽ được thực thi sau mỗi 15 giây (15000 milliseconds).
    private final DeviceThreadManager deviceThreadManager;
    private final IDeviceService deviceService;

    public DeviceDetectionTask(DeviceThreadManager deviceThreadManager,
                               IDeviceService deviceService) {
        this.deviceThreadManager = deviceThreadManager;
        this.deviceService = deviceService;
    }

    @Scheduled(initialDelay = 0, fixedRate = FIXED_RATE)
    public void runTask() {
        log.info("Device detection task started at {}", new Date());
        List<Device> allDevices = deviceService.findAll();
        for (Device device : allDevices) {
            if (deviceThreadManager.getDeviceThread(device.getId()) == null // device is not running
                    && deviceThreadManager.isSimulatedDevice(device.getId())) { // but device should be running
                log.info("Device {} is not running, starting it", device.getName());
                deviceThreadManager.addDeviceThread(device, null);
            }
        }
    }
}
