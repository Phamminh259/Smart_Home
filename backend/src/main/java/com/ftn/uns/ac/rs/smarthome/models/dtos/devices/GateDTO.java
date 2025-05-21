package com.ftn.uns.ac.rs.smarthome.models.dtos.devices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GateDTO extends DeviceDTO {
    @NotNull(message = "{required}")
    public boolean publicMode;

    public List<String> allowedRegistrationPlates;
}
