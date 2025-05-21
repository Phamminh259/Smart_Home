package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Role;
import com.ftn.uns.ac.rs.smarthome.repositories.RoleRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IRoleService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> getByName(String name) {
        return this.roleRepository.findByName(name);
    }
}
