package com.ftn.uns.ac.rs.smarthome.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ACStateChange {

    private Integer userId;
    private Integer deviceId;
    private String change;
    @Nullable
    private Map<String,String> extraInfo;
}
