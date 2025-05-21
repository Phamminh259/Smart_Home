package com.ftn.uns.ac.rs.smarthome.controllers;

import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.UserSearchInfo;
import com.ftn.uns.ac.rs.smarthome.models.dtos.*;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IUserService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

import java.util.List;
import java.util.Locale;
// quản lý người dùng API tiếp nhận request
@CrossOrigin("*")
@RestController
@RequestMapping(value = "/api/user")
@Validated
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final IUserService userService;
    private final MessageSource messageSource;

    public UserController(AuthenticationManager authenticationManager,
                          IUserService userService,
                          MessageSource messageSource) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@Valid @ModelAttribute UserInfoRegister userInfoRegister) {
        try{
            this.userService.register(userInfoRegister);
            return new ResponseEntity<>(messageSource.getMessage("registration.success", null, Locale.getDefault()), HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PostMapping(value = "/registerAdmin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerAdmin(@Valid @ModelAttribute UserInfoRegister userInfoRegister) {
        try{
            this.userService.register(userInfoRegister);
            return new ResponseEntity<>(messageSource.getMessage("registrationAdmin.success", null, Locale.getDefault()), HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }


    @GetMapping(value = "/activate/{userId}")
    public ResponseEntity<?> activateAccount(@PathVariable Integer userId) {
        try{
            this.userService.activate(userId);
            return new ResponseEntity<>(messageSource.getMessage("activation.success", null, Locale.getDefault()), HttpStatus.USE_PROXY);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PostMapping(value = "/sendPasswordResetEmail")
    public ResponseEntity<?> sendPasswordResetEmail(@Valid @RequestBody PasswordResetRequestDTO passwordReset) {
        try{
            this.userService.sendPasswordResetEmail(passwordReset.getEmail());
            return new ResponseEntity<>(messageSource.getMessage("passwordResetEmail.success", null, Locale.getDefault()), HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PostMapping(value = "/passwordReset")
    public ResponseEntity<?> passwordReset(@Valid @RequestBody PasswordResetDTO passwordReset) {
        try{
            this.userService.resetPassword(passwordReset);
            return new ResponseEntity<>(messageSource.getMessage("passwordReset.success", null, Locale.getDefault()), HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @GetMapping(value = "/info")
    public ResponseEntity<?> getUserInfo() {
        try{
            User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserInfoDTO userInfo = this.userService.getUserInfo(user.getId());
            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @GetMapping(value = "/info/{key}")
    public ResponseEntity<?> getUserInfoByKey(@PathVariable String key) {
        try{
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<UserSearchInfo> users = this.userService.findByKey(key, user.getId());
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO authenticationRequest) {
        try {
            TokenDTO generatedToken = userService.login(authenticationRequest);
            return new ResponseEntity<>(generatedToken, HttpStatus.OK);
        }
        catch(UsernameNotFoundException ex) {
            return new ResponseEntity<>(messageSource.getMessage("login.invalid", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
        catch(ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }
}
