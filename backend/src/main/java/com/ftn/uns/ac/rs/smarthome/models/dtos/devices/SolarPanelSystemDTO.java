package com.ftn.uns.ac.rs.smarthome.models.dtos.devices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolarPanelSystemDTO extends DeviceDTO {
    @NotNull(message = "{required}")
    private Integer numberOfPanels;

    @NotNull(message = "{required}")
    private Double panelSize;

    @NotNull(message = "{required}")
    private Double panelEfficiency;
}
