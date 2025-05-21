package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.ChargerDTO;

import java.io.IOException;

public interface IChargerService {
    void register(ChargerDTO chargerDTO, User user) throws IOException;
}
