package com.ftn.uns.ac.rs.smarthome.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// tìm kiếm lịch sử lệnh
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandsRequestDTO {
    private Long from;
    private Long to;
    private Integer deviceId;
    private Long page;
    private Long size;
    private Boolean firstFetch;
    private Integer userId;
}
