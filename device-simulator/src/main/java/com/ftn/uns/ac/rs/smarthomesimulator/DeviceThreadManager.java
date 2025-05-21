package com.ftn.uns.ac.rs.smarthomesimulator;

import com.ftn.uns.ac.rs.smarthomesimulator.models.ACCommand;
import com.ftn.uns.ac.rs.smarthomesimulator.models.Command;
import com.ftn.uns.ac.rs.smarthomesimulator.models.WMCommand;
import com.ftn.uns.ac.rs.smarthomesimulator.models.devices.*;
import com.ftn.uns.ac.rs.smarthomesimulator.repositories.BatteryRepository;
import com.ftn.uns.ac.rs.smarthomesimulator.services.MqttService;
import com.ftn.uns.ac.rs.smarthomesimulator.threads.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
//Quản lý các luồng mô phỏng thiết bị
@Component
public class DeviceThreadManager {
    private final Map<Integer, Thread> deviceThreadMap = new ConcurrentHashMap<>();

    private final Set<Integer> nonSimulatedDevices = ConcurrentHashMap.newKeySet();

    private final MqttService mqttService;
    private final BatteryRepository batteryRepository;

    public DeviceThreadManager(MqttService mqttService,
                               BatteryRepository batteryRepository) {
        this.mqttService = mqttService;
        this.batteryRepository = batteryRepository;
    }

    public void addDeviceThread(Device device, Command command) {
        switch (device.getClass().getSimpleName()) {
            case "Thermometer" -> {
                Thermometer thermometer = (Thermometer) device;
                addDeviceThreadInternal(device.getId(),
                        new ThermometerThread(thermometer, mqttService).getNewSimulatorThread());
            }
            case "WashingMachine" -> {
                WashingMachine wm = (WashingMachine) device;
                addDeviceThreadInternal(device.getId(),
                        new WashingMachineThread(wm, (WMCommand) command).getNewSimulatorThread());
            }
            case "AirConditioner" -> {
                AirConditioner ac = (AirConditioner) device;
                addDeviceThreadInternal(device.getId(),
                        new ACThread(ac, (ACCommand) command).getNewSimulatorThread());
            }
            case "SolarPanelSystem" -> {
                SolarPanelSystem system = (SolarPanelSystem) device;
                addDeviceThreadInternal(device.getId(),
                        new SolarPanelSystemThread(system).getNewSimulatorThread());
            }
            case "Battery" -> {
                Battery battery = (Battery) device;
                addDeviceThreadInternal(device.getId(),
                        new BatteryThread(battery, batteryRepository).getNewSimulatorThread());
            }
            case "Charger" -> {
                Charger charger = (Charger) device;
                addDeviceThreadInternal(device.getId(),
                        new ChargerThread(charger).getNewSimulatorThread());
            }
            default -> {}
        }
    }

    private void addDeviceThreadInternal(Integer deviceId, Thread thread) {
        deviceThreadMap.put(deviceId, thread);
    }

    public Thread getDeviceThread(Integer deviceId) {
        return deviceThreadMap.get(deviceId);
    }

    public boolean isSimulatedDevice(Integer deviceId) {
        return !nonSimulatedDevices.contains(deviceId);
    }

}
