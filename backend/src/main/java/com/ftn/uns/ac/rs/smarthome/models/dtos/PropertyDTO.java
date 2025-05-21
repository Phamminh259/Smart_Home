package com.ftn.uns.ac.rs.smarthome.models.dtos;

import com.ftn.uns.ac.rs.smarthome.models.PropertyStatus;
import com.ftn.uns.ac.rs.smarthome.models.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyDTO {
    private Integer id;
    private String name;
    private String address;
    private String city;
    private String size;
    private String floors;
    private String status;
    private String propertyType;
    private MultipartFile picture;
    private String owner;

    public PropertyDTO(String address, String name, String cityName, String size, String floors, PropertyStatus status, PropertyType propertyType, String username, Integer id) {
        this.address = address;
        this.name = name;
        this.city = cityName;
        this.size = size;
        this.floors = floors;
        this.propertyType = status.toString();
        this.picture = null;
        this.propertyType = propertyType.toString();
        this.owner = username;
        this.id = id;
    }
}