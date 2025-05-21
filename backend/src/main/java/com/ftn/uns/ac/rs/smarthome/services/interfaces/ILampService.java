package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.DeviceDTO;

import java.io.IOException;

public interface ILampService {
    void register(DeviceDTO lampDTO, User user) throws IOException;
}
