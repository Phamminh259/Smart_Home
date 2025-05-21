package com.ftn.uns.ac.rs.smarthomesimulator.models;

import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.ACMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//tham số cho máy lạnh
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ACCommandParams {

    private Integer userId;
    private String unit;
    private Integer target;
    private Integer fanSpeed;
    private Double currentTemp;
    private boolean health;
    private boolean fungus;
    private ACMode mode;
    private boolean everyDay;
    private Long from;
    private Long to;
    private Long taskId;
}
