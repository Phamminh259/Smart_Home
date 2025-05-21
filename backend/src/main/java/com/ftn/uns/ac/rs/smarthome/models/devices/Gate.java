package com.ftn.uns.ac.rs.smarthome.models.devices;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.GateDTO;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="GATES")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Gate extends Device {
    @Column(nullable = false)
    public boolean publicMode;

    @ElementCollection(targetClass=String.class)
    @CollectionTable(name="PLATES", joinColumns=@JoinColumn(name="gate_id"))
    @Column(name="plate", nullable = false)
    public List<String> allowedRegistrationPlates;

    public Gate(GateDTO dto, Property property) {
        super(-1, property, dto.getName(), null, dto.getPowerSource(), dto.getEnergyConsumption(), false, false);
        this.publicMode = dto.isPublicMode();
        this.allowedRegistrationPlates = dto.getAllowedRegistrationPlates();
    }
}
