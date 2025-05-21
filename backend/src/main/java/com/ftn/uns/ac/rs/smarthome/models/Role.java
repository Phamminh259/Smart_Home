package com.ftn.uns.ac.rs.smarthome.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.io.Serial;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="ROLE")
public class Role implements GrantedAuthority {
	@Serial
    private static final long serialVersionUID = 1L;
	@Id
    @Column(unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Getter
    @Column(nullable = false, unique = true)
    String name;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return name;
    }

}
