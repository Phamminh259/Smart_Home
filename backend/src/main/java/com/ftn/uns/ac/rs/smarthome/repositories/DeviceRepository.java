package com.ftn.uns.ac.rs.smarthome.repositories;

import com.ftn.uns.ac.rs.smarthome.models.devices.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {

    @Query("SELECT dvc FROM Device dvc WHERE dvc.property.owner.id = ?1")
    List<Device> findByOwnerId(Integer ownerId);

}
