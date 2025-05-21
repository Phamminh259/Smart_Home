package com.ftn.uns.ac.rs.smarthome.models.dtos.devices;

import com.ftn.uns.ac.rs.smarthome.models.PowerSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDTO {
    @NotNull(message = "{required}")
    private Integer propertyId;

    @Length(max = 64)
    private String name;

    @NotNull
    private MultipartFile image;

    @NotNull
    private PowerSource powerSource;

    private Double energyConsumption;
}
