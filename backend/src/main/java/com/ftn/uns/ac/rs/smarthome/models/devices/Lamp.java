package com.ftn.uns.ac.rs.smarthome.models.devices;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.DeviceDTO;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="LAMPS")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Lamp extends Device {
    private Boolean isOn;

    public Lamp(DeviceDTO dto, Property property) {
        super(-1, property, dto.getName(), null, dto.getPowerSource(), dto.getEnergyConsumption(), false, false);
        this.isOn = false;
    }
}
