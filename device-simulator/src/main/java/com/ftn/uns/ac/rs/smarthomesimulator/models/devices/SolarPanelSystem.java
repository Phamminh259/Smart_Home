package com.ftn.uns.ac.rs.smarthomesimulator.models.devices;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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
}
