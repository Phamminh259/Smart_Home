package com.ftn.uns.ac.rs.smarthomesockets.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WMValueDigest {
    private Integer deviceId;
    private Integer centrifugeSpeed;
    private Integer temperature;
    private String unit;
    private String mode;
}
