package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.dtos.PropertyDTO;

import java.util.List;
import java.util.Optional;

public interface IPropertyService {
    void registerProperty(PropertyDTO propertyDTO);

    List<PropertyDTO> getApprovedProperties(String username);

    Object getAllProperty();

    List<PropertyDTO> getAllApprovedProperties();

    void approveProperty(Integer id);

    void denyProperty(Integer id);

    Object getAllUnapprovedProperty();
    Optional<Property> getById(Integer id);
    List<Integer> getPropertyIdsByCityId(Integer id);
}