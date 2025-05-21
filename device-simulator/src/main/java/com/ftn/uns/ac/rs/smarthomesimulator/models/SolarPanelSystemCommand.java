package com.ftn.uns.ac.rs.smarthomesimulator.models;

import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.CommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//  lệnh điều khiển đến hệ thống pin mặt trờI
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolarPanelSystemCommand extends Command {
    private Integer deviceId;
    private CommandType commandType;
    private ACCommandParams commandParams;
}
