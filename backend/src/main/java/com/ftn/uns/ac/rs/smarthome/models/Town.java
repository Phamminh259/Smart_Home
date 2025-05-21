package com.ftn.uns.ac.rs.smarthome.models;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name="TOWNS")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Town {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer id;

    @Column(nullable = false)
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Property> properties;
}