package com.ftn.uns.ac.rs.smarthome.models.devices;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.ChargerDTO;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="CHARGERS")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Charger extends Device {
    @Column(nullable = false)
    private Double power; // kW

    @Column(nullable = false)
    private Integer numberOfPorts;

    @Column(nullable = false)
    private Double chargeUntil; // % (0, 1]

    public Charger(ChargerDTO dto, Property property) {
        super(-1, property, dto.getName(), null, dto.getPowerSource(), dto.getEnergyConsumption(), false, false);
        this.power = dto.getPower();
        this.numberOfPorts = dto.getNumberOfPorts();
        this.chargeUntil = dto.getChargeUntil();
    }
}
