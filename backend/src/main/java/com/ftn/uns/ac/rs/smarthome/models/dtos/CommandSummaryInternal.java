package com.ftn.uns.ac.rs.smarthome.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;
// đọc từ cơ sở dữ liệu thời gian thực như InfluxDB.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandSummaryInternal {
    private String command;
    private Date timestamp;
    private Map<String, String> tags;

}
