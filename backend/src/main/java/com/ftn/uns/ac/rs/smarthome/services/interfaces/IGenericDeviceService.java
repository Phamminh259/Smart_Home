package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.devices.Device;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.DeviceDTO;

import javax.validation.Valid;
import java.io.IOException;

public interface IGenericDeviceService<D extends Device, DDTO extends DeviceDTO> {
    void register(@Valid DDTO dto, User user) throws IOException;
}
