package com.ftn.uns.ac.rs.smarthome.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {

    private Integer id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String profilePicture;
    private String role;
}
