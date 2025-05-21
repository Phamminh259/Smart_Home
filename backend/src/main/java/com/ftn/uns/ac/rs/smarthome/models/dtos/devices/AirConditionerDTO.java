package com.ftn.uns.ac.rs.smarthome.models.dtos.devices;

import com.ftn.uns.ac.rs.smarthome.models.TemperatureUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
// hướng dữ liệu
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AirConditionerDTO extends DeviceDTO {

    @NotNull(message = "{required}")
    private TemperatureUnit temperatureUnit;

    @NotNull(message = "{required}")
    private Integer maxTemperature;

    @NotNull(message = "{required}")
    private Integer minTemperature;

    @NotNull(message = "{required}")
    private Integer fanSpeed;

    @NotNull(message = "{required}")
    private Boolean cooling;

    @NotNull(message = "{required}")
    private Boolean heating;

    @NotNull(message = "{required}")
    private Boolean dry;

    @NotNull(message = "{required}")
    private Boolean auto;

    @NotNull(message = "{required}")
    private Boolean health;

    @NotNull(message = "{required}")
    private Boolean fungusPrevention;
}
