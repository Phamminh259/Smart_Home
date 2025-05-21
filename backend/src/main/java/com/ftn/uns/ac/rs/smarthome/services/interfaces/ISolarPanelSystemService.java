package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.SolarPanelSystemDTO;

import java.io.IOException;

public interface ISolarPanelSystemService {
    void register(SolarPanelSystemDTO solarPanelSystemDTO, User user) throws IOException;

    void turnOffSolarPanelSystem(Integer id, Integer userId);

    void turnOnSolarPanelSystem(Integer id, Integer userId);
}
