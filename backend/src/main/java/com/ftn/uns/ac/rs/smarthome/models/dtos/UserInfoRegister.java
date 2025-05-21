package com.ftn.uns.ac.rs.smarthome.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoRegister {

    @Length(max = 255, message = "{maxLength}")
    @NotBlank(message = "{required}")
    private String username;

    @Length(max = 255, message = "{maxLength}")
    @NotBlank(message = "{required}")
    private String name;

    @Length(max = 255, message = "{maxLength}")
    @NotBlank(message = "{required}")
    private String surname;

    @NotBlank
    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "{format}")
    private String email;

    @Length(max = 255, message = "{maxLength}")
    @Length(min = 8, message = "{minLength}")
    @NotBlank(message = "{required}")
    private String password;

    @Length(max = 255, message = "{maxLength}")
    @NotBlank(message = "{required}")
    private String role;

    private MultipartFile profilePicture;
}
