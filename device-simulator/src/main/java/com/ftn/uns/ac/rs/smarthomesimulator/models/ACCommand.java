package com.ftn.uns.ac.rs.smarthomesimulator.models;

import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.CommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//lệnh gửi đến máy lạnh.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ACCommand extends Command {

    private Integer deviceId;
    private CommandType commandType;
    private ACCommandParams commandParams;
}
