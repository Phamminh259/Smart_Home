package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.WashingMachineDTO;

import java.io.IOException;

public interface IWashingMachineService {
    void register(WashingMachineDTO washingMachineDTO, User user) throws IOException;
}
