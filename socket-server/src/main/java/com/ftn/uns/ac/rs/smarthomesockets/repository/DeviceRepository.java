package com.ftn.uns.ac.rs.smarthomesockets.repository;

import com.ftn.uns.ac.rs.smarthomesockets.models.devices.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {}
