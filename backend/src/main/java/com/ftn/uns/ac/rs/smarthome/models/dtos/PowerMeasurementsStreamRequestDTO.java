package com.ftn.uns.ac.rs.smarthome.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PowerMeasurementsStreamRequestDTO {

    @NotNull(message = "{required}")
    @Min(0)
    private Long from;
    @NotNull(message = "{required}")
    @Min(0)
    private Long to;
    @NotNull(message = "{required}")
    @Min(0)
    private Integer limit;
    @NotNull(message = "{required}")
    @Min(0)
    private Integer offset;
    @NotNull(message = "{required}")
    private List<Integer> deviceIds;
    @NotBlank(message = "{required}")
    private String measurementName;
}
