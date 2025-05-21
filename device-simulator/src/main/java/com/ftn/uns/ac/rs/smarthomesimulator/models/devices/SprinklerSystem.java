package com.ftn.uns.ac.rs.smarthomesimulator.models.devices;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Time;
//Hệ thống tưới
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
}
