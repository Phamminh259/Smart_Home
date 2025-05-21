package com.ftn.uns.ac.rs.smarthome.services.interfaces;

import com.ftn.uns.ac.rs.smarthome.models.Role;

import java.util.Optional;

public interface IRoleService {

    Optional<Role> getByName(String name);
}
