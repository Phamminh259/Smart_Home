package com.ftn.uns.ac.rs.smarthome.repositories;

import com.ftn.uns.ac.rs.smarthome.models.devices.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatteryRepository extends JpaRepository<Battery, Integer> {
    @Query("SELECT b FROM Battery b WHERE b.capacity > b.occupiedCapacity AND b.property.id = ?1")
    List<Battery> findAllNonFull(int propertyId);

    List<Battery> findAllByPropertyIdAndOccupiedCapacityGreaterThan(int propertyId, double occupiedCapacity);
}
