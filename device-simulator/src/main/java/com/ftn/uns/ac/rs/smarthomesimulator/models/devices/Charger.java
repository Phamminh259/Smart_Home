package com.ftn.uns.ac.rs.smarthomesimulator.models.devices;

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
}
