package com.ftn.uns.ac.rs.smarthomesimulator.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
// mo ta thay đổi trạng thái máy lạnh
@Data
@AllArgsConstructor
public class ACStateChange {

    private Integer userId;
    private Integer deviceId;
    private String change;
    private Map<String,String> extraInfo;
}
