package com.ftn.uns.ac.rs.smarthome.controllers;

import com.ftn.uns.ac.rs.smarthome.models.dtos.PropertyDTO;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IPropertyService;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IUserService;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/api/property")
@Validated
public class PropertyController {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private final AuthenticationManager authenticationManager;
    private final IUserService userService;
    private final MessageSource messageSource;
    private final IPropertyService propertyService;

    public PropertyController(AuthenticationManager authenticationManager,
                              IUserService userService,
                              MessageSource messageSource,
                              IPropertyService propertyService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.messageSource = messageSource;
        this.propertyService = propertyService;
    }

    @PostMapping(value = "/registerProperty", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@Valid @ModelAttribute PropertyDTO propertyDTO) {
        try{
            this.propertyService.registerProperty(propertyDTO);
            return new ResponseEntity<>(messageSource.getMessage("registrationProperty.success", null, Locale.getDefault()), HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @GetMapping(value = "/approvedProperties/{username}")
    public ResponseEntity<?> getApprovedProperties(@PathVariable String username){
        try{
//            log.info("Fetching approved properties for user: " + username);
            List<PropertyDTO> approvedProperties = this.propertyService.getApprovedProperties(username);
//            log.info("Fetched approved properties for user: " + username + " - " + approvedProperties.size() + " properties");
            return new ResponseEntity<>(approvedProperties, HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @GetMapping(value = "/allProperties")
    public ResponseEntity<?> getAllProperties(){
        try{
            return new ResponseEntity<>(this.propertyService.getAllProperty(), HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @GetMapping(value = "/approvedProperties")
    public ResponseEntity<?> getAllApprovedProperties(){
        try{
            return new ResponseEntity<>(this.propertyService.getAllApprovedProperties(), HttpStatus.OK);
        } catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @GetMapping(value = "/unapprovedProperties")
    public ResponseEntity<?> getAllUnapprovedProperties(){
        try{
            return new ResponseEntity<>(this.propertyService.getAllUnapprovedProperty(), HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PutMapping(value = "/approve/{propertyId}")
    public ResponseEntity<?> approveProperty(@PathVariable Integer propertyId){
        try{
            this.propertyService.approveProperty(propertyId);
            return new ResponseEntity<>(messageSource.getMessage("propertyApproved.success", null, Locale.getDefault()), HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PutMapping(value = "/deny/{propertyId}")
    public ResponseEntity<?> denyProperty(@PathVariable Integer propertyId){
        try{
            this.propertyService.denyProperty(propertyId);
            return new ResponseEntity<>(messageSource.getMessage("propertyDeny.success", null, Locale.getDefault()), HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }
}