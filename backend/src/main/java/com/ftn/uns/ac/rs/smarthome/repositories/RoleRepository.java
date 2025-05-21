package com.ftn.uns.ac.rs.smarthome.repositories;

import com.ftn.uns.ac.rs.smarthome.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
	Optional<Role> findByName(String name);
}
