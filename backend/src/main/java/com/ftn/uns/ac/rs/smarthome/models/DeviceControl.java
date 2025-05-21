package com.ftn.uns.ac.rs.smarthome.models;


import com.ftn.uns.ac.rs.smarthome.models.devices.Device;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="DEVICE_CONTROL", indexes = {@Index(name = "deviceIdx", columnList = "device_id"),
                                         @Index(name = "ownerId", columnList = "owner_id")})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DeviceControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique=true, nullable=false)
    private Integer id;

    @ManyToOne
    private User owner;

    @ManyToOne
    private Device device;

    public DeviceControl(User owner, Device device) {
        this.owner = owner;
        this.device = device;
    }
}
