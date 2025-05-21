package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.Measurement;
import com.ftn.uns.ac.rs.smarthome.models.devices.Device;
import com.ftn.uns.ac.rs.smarthome.models.dtos.*;

import java.util.List;
import java.util.Optional;

public interface IDeviceService {

    Optional<Device> getById(Integer id);
    List<Device> findAll();
    List<DeviceDetailsDTO> findByOwnerId(Integer ownerId);
    void update(Device device);
    void setDeviceStillThere(int id);
    List<List<Measurement>> getStreamByMeasurementNameAndDeviceIdInTimeRange(MeasurementsStreamRequestDTO requestDTO);
    List<List<Measurement>> findPowerAggregation(PowerMeasurementsStreamRequestDTO requestDTO);
    CommandsDTO getCommandsByTimeRangeAndUserId(CommandsRequestDTO request);

}
