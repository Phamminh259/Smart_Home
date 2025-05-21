package com.ftn.uns.ac.rs.smarthomesockets.models;

import com.ftn.uns.ac.rs.smarthomesockets.models.enums.ACMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandParams {
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
