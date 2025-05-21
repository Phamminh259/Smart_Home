package com.ftn.uns.ac.rs.smarthomesockets.models;

import com.ftn.uns.ac.rs.smarthomesockets.models.enums.CommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WMCommand extends Command{
    private Integer deviceId;
    private CommandType commandType;
    private WMCommandParams commandParams;
}
