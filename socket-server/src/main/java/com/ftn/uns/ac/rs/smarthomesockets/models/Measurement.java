package com.ftn.uns.ac.rs.smarthomesockets.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Measurement {
    private String name;
    private double value;
    private Long timestamp;
    private Map<String,String> tags;
}
