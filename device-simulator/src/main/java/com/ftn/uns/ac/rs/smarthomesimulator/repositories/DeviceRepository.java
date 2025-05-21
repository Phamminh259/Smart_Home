package com.ftn.uns.ac.rs.smarthomesimulator.repositories;

import com.ftn.uns.ac.rs.smarthomesimulator.models.devices.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {

}
