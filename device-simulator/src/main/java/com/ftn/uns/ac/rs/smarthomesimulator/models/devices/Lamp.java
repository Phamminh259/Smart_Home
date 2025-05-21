package com.ftn.uns.ac.rs.smarthomesimulator.models.devices;

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
}
