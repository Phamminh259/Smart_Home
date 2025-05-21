package com.ftn.uns.ac.rs.smarthome.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// lệnh đã được gửi đến thiết bị

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandSummary {
    private Long timestamp;
    private String username;
    private String command;
}
