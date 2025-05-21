package com.ftn.uns.ac.rs.smarthome.models.devices;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.SprinklerSystemDTO;
import lombok.*;
import org.hibernate.annotations.Cache;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Time;

@Entity
@Table(name="SPRINKLER_SYSTEMS")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SprinklerSystem extends Device {
    @Column(nullable = false)
    public boolean isOn;

    @Column(nullable = false)
    public boolean specialMode;

    @Column
    public Time startTime;

    @Column
    public Time endTime;

    public SprinklerSystem(SprinklerSystemDTO dto, Property property) {
        super(-1, property, dto.getName(), null, dto.getPowerSource(), dto.getEnergyConsumption(), false, false);
        this.isOn = false;
        this.specialMode = dto.isSpecialMode();
        this.startTime = dto.getStartTime();
        this.endTime = dto.getEndTime();
    }
}
