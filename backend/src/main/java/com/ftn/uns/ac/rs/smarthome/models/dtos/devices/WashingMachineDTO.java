package com.ftn.uns.ac.rs.smarthome.models.dtos.devices;

import com.ftn.uns.ac.rs.smarthome.models.TemperatureUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WashingMachineDTO extends DeviceDTO {

    @NotNull(message = "{required}")
    private Boolean cottons;

    @NotNull(message = "{required}")
    private Boolean synthetics;

    @NotNull(message = "{required}")
    private Boolean dailyExpress;

    @NotNull(message = "{required}")
    private Boolean wool;

    @NotNull(message = "{required}")
    private Boolean darkWash;

    @NotNull(message = "{required}")
    private Boolean outdoor;

    @NotNull(message = "{required}")
    private Boolean shirts;

    @NotNull(message = "{required}")
    private Boolean duvet;

    @NotNull(message = "{required}")
    private Boolean mixed;

    @NotNull(message = "{required}")
    private Boolean steam;

    @NotNull(message = "{required}")
    private Boolean rinseAndSpin;

    @NotNull(message = "{required}")
    private Boolean spinOnly;

    @NotNull(message = "{required}")
    private Boolean hygiene;

    @NotNull(message = "{required}")
    private Integer centrifugeMin;

    @NotNull(message = "{required}")
    private Integer centrifugeMax;

    @NotNull(message = "{required}")
    private Integer temperatureMin;

    @NotNull(message = "{required}")
    private Integer temperatureMax;

    @NotNull(message = "{required}")
    private TemperatureUnit temperatureUnit;
}
