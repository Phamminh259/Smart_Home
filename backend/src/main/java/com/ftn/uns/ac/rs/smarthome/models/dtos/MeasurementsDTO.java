package com.ftn.uns.ac.rs.smarthome.models.dtos;

import com.ftn.uns.ac.rs.smarthome.models.Measurement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeasurementsDTO {

    private List<Measurement> batch;
    private boolean hasMore;
}
