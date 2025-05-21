package com.ftn.uns.ac.rs.smarthome.models.dtos.devices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatteryDTO extends DeviceDTO {
    @NotNull(message = "{required}")
    @Positive(message = "{positive}")
    private Double capacity;
}
