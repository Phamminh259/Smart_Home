package com.ftn.uns.ac.rs.smarthome.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceControlDetails {

    @NotNull(message = "{required}")
    private Integer userId;

    @NotBlank(message = "{required}")
    private String action;
}
