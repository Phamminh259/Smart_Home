package com.ftn.uns.ac.rs.smarthome.models;

import com.ftn.uns.ac.rs.smarthome.models.devices.Device;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="PROPERTY_CONTROL")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropertyControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique=true, nullable=false)
    private Integer id;

    @ManyToOne
    private User owner;

    @ManyToOne
    private Property property;

    public PropertyControl(User owner, Property property) {
        this.owner = owner;
        this.property = property;
    }
}
