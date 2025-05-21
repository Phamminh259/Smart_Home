package com.ftn.uns.ac.rs.smarthomesimulator.models;

import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.CommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//Gửi lệnh đến máy giặt
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WMCommand extends Command{
    private Integer deviceId;
    private CommandType commandType;
    private WMCommandParams commandParams;
}
