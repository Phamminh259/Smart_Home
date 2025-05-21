package com.ftn.uns.ac.rs.smarthome.models.devices;


import com.ftn.uns.ac.rs.smarthome.models.PowerSource;
import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.TemperatureUnit;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.WashingMachineDTO;
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

    public WashingMachine(WashingMachineDTO dto, Property property) {
        super(-1, property, dto.getName(), null, dto.getPowerSource(), dto.getEnergyConsumption(), false, false);
        this.cottons = dto.getCottons();
        this.synthetics = dto.getSynthetics();
        this.dailyExpress = dto.getDailyExpress();
        this.wool = dto.getWool();
        this.darkWash = dto.getDarkWash();
        this.outdoor = dto.getOutdoor();
        this.shirts = dto.getShirts();
        this.duvet = dto.getDuvet();
        this.mixed = dto.getMixed();
        this.steam = dto.getSteam();
        this.rinseAndSpin = dto.getRinseAndSpin();
        this.spinOnly = dto.getSpinOnly();
        this.hygiene = dto.getHygiene();
        this.centrifugeMin = dto.getCentrifugeMin();
        this.centrifugeMax = dto.getCentrifugeMax();
        this.temperatureMin = dto.getTemperatureMin();
        this.temperatureMax = dto.getTemperatureMax();
        this.temperatureUnit = dto.getTemperatureUnit();
        this.isOn = false;
    }
}
