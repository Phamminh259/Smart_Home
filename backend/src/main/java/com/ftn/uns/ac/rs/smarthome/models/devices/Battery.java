package com.ftn.uns.ac.rs.smarthome.models.devices;

import com.ftn.uns.ac.rs.smarthome.models.PowerSource;
import com.ftn.uns.ac.rs.smarthome.models.Property;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="BATTERIES")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Battery extends Device {
    @Column(nullable = false)
    private Double capacity;

    @Column(nullable = false)
    private Double occupiedCapacity;

    public Battery(Property property, String name,
                   PowerSource powerSource, Double energyConsumption, Double capacity) {
        super(-1, property, name, null, powerSource, energyConsumption, false, false);
        this.capacity = capacity;
        this.occupiedCapacity = 0.0;
    }

    public double getRemainingCapacity() {
        return capacity - occupiedCapacity;
    }
}
