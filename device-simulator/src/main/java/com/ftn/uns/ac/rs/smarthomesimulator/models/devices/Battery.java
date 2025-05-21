package com.ftn.uns.ac.rs.smarthomesimulator.models.devices;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="BATTERIES")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Battery extends Device {
    @Column(nullable = false)
    private Double capacity;

    @Column(nullable = false)
    private Double occupiedCapacity;
}
