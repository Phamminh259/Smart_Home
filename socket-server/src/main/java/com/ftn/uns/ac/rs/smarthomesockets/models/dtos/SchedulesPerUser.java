package com.ftn.uns.ac.rs.smarthomesockets.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchedulesPerUser {
    private Integer deviceId;
    private List<Scheduled> schedules;
}
