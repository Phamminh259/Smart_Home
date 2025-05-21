package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.TemperatureUnit;
import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.ThermometerDTO;

import java.io.IOException;

public interface IThermometerService {
    void register(ThermometerDTO thermometerDTO, User user) throws IOException;
    void changeThermometerTempUnit(Integer id, TemperatureUnit unit);
}
