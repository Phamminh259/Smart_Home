package com.ftn.uns.ac.rs.smarthome.repositories;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.PropertyStatus;
import com.ftn.uns.ac.rs.smarthome.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Integer> {
    Optional<Property> findByAddress(String address);
    Optional<Property> findByName(String name);
    List<Property> findAllByOwner(User owner);
    List<Property> findAllByStatus(PropertyStatus status);
}
