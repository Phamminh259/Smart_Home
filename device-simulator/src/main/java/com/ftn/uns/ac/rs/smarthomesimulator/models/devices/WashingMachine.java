package com.ftn.uns.ac.rs.smarthomesimulator.models.devices;


import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.TemperatureUnit;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="WASHING_MACHINES")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WashingMachine extends Device {
    @Column(nullable = false)
    private Boolean cottons;

    @Column(nullable = false)
    private Boolean synthetics;

    @Column(nullable = false)
    private Boolean dailyExpress;

    @Column(nullable = false)
    private Boolean wool;

    @Column(nullable = false)
    private Boolean darkWash;

    @Column(nullable = false)
    private Boolean outdoor;

    @Column(nullable = false)
    private Boolean shirts;

    @Column(nullable = false)
    private Boolean duvet;

    @Column(nullable = false)
    private Boolean mixed;

    @Column(nullable = false)
    private Boolean steam;

    @Column(nullable = false)
    private Boolean rinseAndSpin;

    @Column(nullable = false)
    private Boolean spinOnly;

    @Column(nullable = false)
    private Boolean hygiene;

    @Column(nullable = false)
    private Integer centrifugeMin;

    @Column(nullable = false)
    private Integer centrifugeMax;

    @Column(nullable = false)
    private Integer temperatureMin;

    @Column(nullable = false)
    private Integer temperatureMax;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TemperatureUnit temperatureUnit;

    @Column(nullable = false)
    private Boolean isOn;
}
