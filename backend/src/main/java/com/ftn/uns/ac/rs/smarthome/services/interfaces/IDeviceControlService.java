package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.UserSearchInfo;
import com.ftn.uns.ac.rs.smarthome.models.dtos.DeviceControlDetails;
import com.ftn.uns.ac.rs.smarthome.models.dtos.DeviceDetailsDTO;

import java.util.List;

public interface IDeviceControlService {

    void editDeviceControl(Integer deviceId, List<DeviceControlDetails> details);
    void editDeviceControlForProperty(Integer propertyId, List<DeviceControlDetails> details);
    List<UserSearchInfo> getDeviceControlUserInfo(Integer deviceId);
    List<DeviceDetailsDTO> findByShared(Integer userId);
    List<UserSearchInfo> getDeviceControlUserInfoForProperty(Integer propertyId);
}
