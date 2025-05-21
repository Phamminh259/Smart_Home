package com.ftn.uns.ac.rs.smarthome.models.dtos;

import com.ftn.uns.ac.rs.smarthome.models.PowerSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDetailsDTO {

    private Integer id;
    private String type;
    private String name;
    private PowerSource powerSource;
    private Double energyConsumption;
    private String picture;
    private String propertyName;
}
