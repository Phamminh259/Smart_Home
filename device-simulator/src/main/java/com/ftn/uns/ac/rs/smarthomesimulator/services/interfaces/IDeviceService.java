package com.ftn.uns.ac.rs.smarthomesimulator.services.interfaces;

import com.ftn.uns.ac.rs.smarthomesimulator.models.devices.Device;

import java.util.List;
import java.util.Optional;

public interface IDeviceService {
    List<Device> findAll();
    Optional<Device> findById(int id);
}
