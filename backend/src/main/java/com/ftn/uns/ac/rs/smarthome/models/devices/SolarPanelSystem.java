package com.ftn.uns.ac.rs.smarthome.models.devices;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.SolarPanelSystemDTO;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
//
@Entity
@Table(name="SOLAR_PANEL_SYSTEMS")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SolarPanelSystem extends Device {
    @Column(nullable = false)
    private Integer numberOfPanels;

    @Column(nullable = false)
    private Double panelSize;

    @Column(nullable = false)
    private Double panelEfficiency; // % in range [0, 1] (0% - 100%) - how much of the sun's energy is converted to electricity

    @Column(nullable = false)
    private Boolean isOn;

    public SolarPanelSystem(SolarPanelSystemDTO dto, Property property) {
        super(-1, property, dto.getName(), null, dto.getPowerSource(), dto.getEnergyConsumption(), false, false);
        this.numberOfPanels = dto.getNumberOfPanels();
        this.panelSize = dto.getPanelSize();
        this.panelEfficiency = dto.getPanelEfficiency();
        this.isOn = false;
    }
}
