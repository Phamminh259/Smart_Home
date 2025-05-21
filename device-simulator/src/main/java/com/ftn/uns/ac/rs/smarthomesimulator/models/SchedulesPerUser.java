package com.ftn.uns.ac.rs.smarthomesimulator.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
// lịch đặt cho từng thiết bị
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchedulesPerUser {
    private Integer deviceId;
    private List<Scheduled> schedules;
}
