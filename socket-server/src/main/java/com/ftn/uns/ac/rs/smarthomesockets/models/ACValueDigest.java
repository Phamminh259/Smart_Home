package com.ftn.uns.ac.rs.smarthomesockets.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ACValueDigest {
    private Integer deviceId;
    private Double currentTemp;
    private Integer targetTemp;
    private String unit;
    private String mode;
    private Integer fanSpeed;
    private boolean health;
    private boolean fungusPrevent;
}
