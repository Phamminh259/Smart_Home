package com.ftn.uns.ac.rs.smarthomesimulator.models.devices;

import lombok.*;

import javax.persistence.*;
import java.util.List;
//Cổng ra vào
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
    @Column(name="book", nullable = false)
    public List<String> allowedRegistrationPlates;
}
