package com.ftn.uns.ac.rs.smarthomesockets.models;

import com.ftn.uns.ac.rs.smarthomesockets.models.enums.CommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ACCommand extends Command {

    private Integer deviceId;
    private CommandType commandType;
    private CommandParams commandParams;
}
