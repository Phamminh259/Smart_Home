package com.ftn.uns.ac.rs.smarthome.models.dtos.devices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargerDTO extends DeviceDTO {
    @NotNull(message = "{required}")
    @Positive(message = "{positive}")
    private Double power; // kW

    @NotNull(message = "{required}")
    @Positive(message = "{positive}")
    private Integer numberOfPorts;

    @NotNull(message = "{required}")
    private Double chargeUntil; // % (0, 1]
}
