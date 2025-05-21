package com.ftn.uns.ac.rs.smarthome.models.devices;

import com.ftn.uns.ac.rs.smarthome.models.PowerSource;
import com.ftn.uns.ac.rs.smarthome.models.Property;
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
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "device_generator")
    @TableGenerator(name = "device_generator", allocationSize = 1)
    @Column(unique = true, nullable = false)
    private Integer id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "property_id", referencedColumnName = "id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private String name;

    @Column
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PowerSource powerSource;

    @Column
    private Double energyConsumption;

    @Column(nullable = false)
    private boolean stillThere = false;

    @Column(nullable = false)
    private boolean online = false;
}
