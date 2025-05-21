package com.ftn.uns.ac.rs.smarthomesockets.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DeviceCapabilities {
    private Map<String,String> capabilities;
}
