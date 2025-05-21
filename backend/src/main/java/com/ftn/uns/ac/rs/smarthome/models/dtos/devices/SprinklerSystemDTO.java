package com.ftn.uns.ac.rs.smarthome.models.dtos.devices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.sql.Time;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SprinklerSystemDTO extends DeviceDTO {
    @NotNull(message = "{required}")
    public boolean specialMode;

    public Time startTime;

    public Time endTime;
}
