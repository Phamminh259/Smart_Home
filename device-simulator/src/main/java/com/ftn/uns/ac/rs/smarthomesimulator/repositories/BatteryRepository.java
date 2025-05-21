package com.ftn.uns.ac.rs.smarthomesimulator.repositories;

import com.ftn.uns.ac.rs.smarthomesimulator.models.devices.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatteryRepository extends JpaRepository<Battery, Integer> {

}
