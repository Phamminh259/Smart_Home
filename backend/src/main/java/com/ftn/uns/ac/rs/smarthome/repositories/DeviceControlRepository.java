package com.ftn.uns.ac.rs.smarthome.repositories;

import com.ftn.uns.ac.rs.smarthome.models.DeviceControl;
import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.devices.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceControlRepository extends JpaRepository<DeviceControl, Integer> {
    List<DeviceControl> findByDevice_Id(Integer id);
    Optional<DeviceControl> findByDevice_IdAndOwner_Id(Integer deviceId, Integer ownerId);
    List<DeviceControl> findByDevice_Property_Id(Integer propertyId);
    List<DeviceControl> findByOwner_Id(Integer ownerId);
}
