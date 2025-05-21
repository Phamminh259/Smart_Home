package com.ftn.uns.ac.rs.smarthome.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Measurement {
    private String name;
    private double value;
    private Date timestamp;
    private Map<String, String> tags;
}
