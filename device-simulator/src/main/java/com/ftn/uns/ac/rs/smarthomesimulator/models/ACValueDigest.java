package com.ftn.uns.ac.rs.smarthomesimulator.models;

import lombok.AllArgsConstructor;
import lombok.Data;
//trạng thái hiện tại máy lạnh
@Data
@AllArgsConstructor
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
