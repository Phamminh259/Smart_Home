package com.ftn.uns.ac.rs.smarthomesimulator.models.devices;

import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.PowerSource;
import com.ftn.uns.ac.rs.smarthomesimulator.models.Property;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="DEVICES")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Device {
    @Id
    @Column(unique = true, nullable = false)
    private Integer id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "property_id", referencedColumnName = "id", nullable = false)
    private Property property;

    @Column
    private String name;

    @Column
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PowerSource powerSource;

    @Column
    private Double energyConsumption;

    @Column(nullable = false)
    private boolean stillThere;

    @Column(nullable = false)
    private boolean online;
}
