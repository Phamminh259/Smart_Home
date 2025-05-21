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
public class ThermometerDTO extends DeviceDTO {
    @NotNull(message = "{required}")
    private TemperatureUnit temperatureUnit;
}
