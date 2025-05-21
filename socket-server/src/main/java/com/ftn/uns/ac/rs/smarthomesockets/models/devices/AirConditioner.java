package com.ftn.uns.ac.rs.smarthomesockets.models.devices;

import com.ftn.uns.ac.rs.smarthomesockets.models.enums.TemperatureUnit;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="AIR_CONDITIONERS")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AirConditioner extends Device {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TemperatureUnit temperatureUnit;

    @Column(nullable = false)
    private Integer maxTemperature;

    @Column(nullable = false)
    private Integer minTemperature;

    @Column(nullable = false)
    private Integer fanSpeed;

    @Column(nullable = false)
    private Boolean cooling;

    @Column(nullable = false)
    private Boolean heating;

    @Column(nullable = false)
    private Boolean dry;

    @Column(nullable = false)
    private Boolean auto;

    @Column(nullable = false)
    private Boolean health;

    @Column(nullable = false)
    private Boolean fungusPrevention;

    @Column(nullable = false)
    private Boolean isOn;
}
