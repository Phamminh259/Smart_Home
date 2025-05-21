package com.ftn.uns.ac.rs.smarthome.models;

import com.ftn.uns.ac.rs.smarthome.models.devices.Device;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="PROPERTIES")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String size;
    @Column(nullable = false)
    private String floors;
    private PropertyStatus status;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String picture;
    @Column(nullable = false)
    private PropertyType propertyType;

    @ManyToOne
    private User owner;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "property")
    private List<Device> devices;

    public Property(String address, String size, String picture, User owner, String floors, String propertyType, long name){
        this.address = address;
        this.size = size;
        this.status = PropertyStatus.UNAPPROVED;
        this.picture = picture;
        this.owner = owner;
        this.floors = floors;
        this.devices = new ArrayList<>();
        if(propertyType == "HOUSE")
            this.propertyType = PropertyType.HOUSE;
        else
            this.propertyType = PropertyType.APARTMENT;
        this.name = Long.toString(name);
    }
}
