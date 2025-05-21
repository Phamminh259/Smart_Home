// @ts-nocheck
import {Button, Grid, Stack, TextField, Typography} from "@mui/material";
import React, {useEffect, useState} from "react";
import './Registration.css'
import {NewUserMultipart} from "../../models/NewUserMultipart";
import {PopupMessage} from "../PopupMessage/PopupMessage";
import {useForm} from "react-hook-form";
import {UserService} from "../../services/UserService";
import {RoleEnum} from "../../models/enums/RoleEnum.ts";

type RegistrationForm = {
    username: string,
    name: string,
    surname: string,
    email: string,
    password: string,
    passwordConfirmation: string
}

interface RegistrationProps {
    userService: UserService
    adminRegistration: boolean
}
export function Registration({userService, adminRegistration} : RegistrationProps) {
    const [selectedImage, setSelectedImage] = useState(new File([], "init"));
    const defaultPictureUrl = "https://t3.ftcdn.net/jpg/05/11/52/90/360_F_511529094_PISGWTmlfmBu1g4nocqdVKaHBnzMDWrN.jpg"
    const [imageUrl, setImageUrl] = useState(defaultPictureUrl);
    const [errorMessage, setErrorMessage] = React.useState('');
    const [errorPopupOpen, setErrorPopupOpen] = React.useState(false);
    const [isSuccess, setIsSuccess] = React.useState(true);
    const {register, getValues, handleSubmit, formState: {errors}} = useForm<RegistrationForm>({
        defaultValues: {
            username: "",
            name: "",
            surname: "",
            email: "",
            password: "",
            passwordConfirmation: ""
        },
        mode: "onChange"
    });

    const onSubmit = (formData : RegistrationForm) => registerUser(formData)
    useEffect(() => {
        if (selectedImage && selectedImage.name != 'init') {
            setImageUrl(URL.createObjectURL(selectedImage));
        }
    }, [selectedImage]);

    function registerUser(formData : RegistrationForm) {
        if (imageUrl == defaultPictureUrl) {
            setErrorMessage("Please select a profile picture!");
            setIsSuccess(false);
            setErrorPopupOpen(true);
            return;
        }
        if (selectedImage.size > 25000000) {
            setErrorMessage("File too large!");
            setIsSuccess(false);
            setErrorPopupOpen(true);
            return;
        }
        if (!selectedImage.type.includes("image")) {
            setErrorMessage("File is not an image!");
            setIsSuccess(false);
            setErrorPopupOpen(true);
            return;
        }
        const role = adminRegistration? RoleEnum.ROLE_ADMIN : RoleEnum.ROLE_USER
        const newUser: NewUserMultipart = {
            username: formData.username.trim(),
            name: formData.name.trim(),
            surname: formData.surname.trim(),
            email: formData.email.trim(),
            password: formData.password.trim(),
            profilePicture: selectedImage,
            role: role
        };
        userService.registerUser(newUser).then((response) => {
            setErrorMessage(response);
            setIsSuccess(true);
            setErrorPopupOpen(true);
        }).catch((error) => {
            if (error.code == 'ERR_NETWORK') {
                setErrorMessage("File too large! Please select a file smaller than 5MB!");
            } else {
                setErrorMessage(error.response.data);
            }
            setIsSuccess(false);
            setErrorPopupOpen(true);
        });
    }

    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);
    };

    return (
        <>
            <form onSubmit={handleSubmit(onSubmit)} className={'height-100 overflow'}>
                <Grid container
                      direction={'row'}
                      justifyContent={"center"}>
                    <Grid item xs={12} sm={12} md={12} lg={12} xl={12}>
                        <Typography variant="h2" mb={5} fontWeight={400}>Sign-up</Typography>
                    </Grid>
                    <br/>
                    <br/>
                    <Grid item container xs={12} sm={12} md={12} lg={12} xl={12} rowSpacing={3}>
                        <Grid item container xs={12} sm={12} md={7} lg={7} xl={7} justifyContent={'center'}>
                            <Grid item xs={10} sm={10} md={8} lg={8} xl={8}>
                                <Grid item container spacing={1}>
                                    <Grid item xs={12} sm={6} md={12} lg={12} xl={12}>
                                        <TextField id="username" label="Username"
                                                   type="text"
                                                   {...register("username",
                                                       {required: "Name is a required field!"})}
                                                   error={!!errors.username}
                                                   helperText={errors.username ? errors.username?.message : "Required"}
                                                   variant="outlined" fullWidth={true}/>
                                    </Grid>
                                    <Grid item xs={12} sm={6} md={12} lg={6} xl={6}>
                                        <TextField id="name" label="Name"
                                                   type="text"
                                                   {...register("name",
                                                       {required: "Name is a required field!"})}
                                                   error={!!errors.name}
                                                   helperText={errors.name ? errors.name?.message : "Required"}
                                                   variant="outlined" fullWidth={true}/>
                                    </Grid>
                                    <Grid item xs={12} sm={6} md={12} lg={6} xl={6}>
                                        <TextField id="surname" label="Surname"
                                                   type="text"
                                                   {...register("surname",
                                                       {required: "Surname is a required field!"})}
                                                   error={!!errors.name}
                                                   helperText={errors.surname ? errors.surname?.message : "Required"}
                                                   variant="outlined" fullWidth={true}/>
                                    </Grid>
                                    <Grid item xs={12} sm={6} md={12} lg={12} xl={12}>
                                        <TextField id="email" label="Email"
                                                   {...register("email",
                                                       {
                                                           required: "Email is a required field!",
                                                           validate: {
                                                               matchPattern: (v) =>
                                                                   /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/.test(v) ||
                                                                   "Email is not in the correct format! The correct format is [example@mail.com]",
                                                           }
                                                       })}
                                                   error={!!errors.email}
                                                   helperText={errors.email ? errors.email?.message : "Format: [example@mail.com]"}
                                                   variant="outlined" fullWidth={true}/>
                                    </Grid>
                                    <Grid item xs={12} sm={6} md={12} lg={12} xl={12}>
                                        <TextField id="password" label="Password"
                                                   type="password"
                                                   {...register("password",
                                                       {
                                                           required: "Password is a required field!",
                                                           minLength: {
                                                               value: 8,
                                                               message: "Password must be at least 8 characters long!"
                                                           }
                                                       })}
                                                   error={!!errors.password}
                                                   helperText={errors.password ? errors.password?.message : "Required, minimum 8 characters"}
                                                   variant="outlined" fullWidth={true}/>
                                    </Grid>
                                    <Grid item xs={12} sm={6} md={12} lg={12} xl={12}>
                                        <TextField id="passwordConfirmation" label="Confirm password"
                                                   type="password"
                                                   {...register("passwordConfirmation",
                                                       {validate: value => value === getValues("password") || "Passwords do not match!"}
                                                   )}
                                                   error={!!errors.passwordConfirmation}
                                                   helperText={errors.passwordConfirmation ? errors.passwordConfirmation?.message : "Required, must be the same as the password above"}
                                                   variant="outlined" fullWidth={true}/>
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                        <Grid item container xs={12} sm={12} md={5} lg={5} xl={5} alignItems={'center'} justifyContent={'center'}>
                            <Stack alignItems={'center'} spacing={3}>
                                <img src={imageUrl} className={'img'} alt={'Profile picture'}/>
                                <input
                                    accept="image/*"
                                    type="file"
                                    id="select-image"
                                    style={{display: "none"}}
                                    onChange={(e) => setSelectedImage(e.target!.files?.[0] as File)}
                                />
                                <label htmlFor="select-image">
                                    <Button variant="contained" color="secondary" component="span">
                                        Upload Image
                                    </Button>
                                </label>
                            </Stack>
                        </Grid>
                    </Grid>
                    <Grid item mt={4}>
                        <Button variant="contained" type="submit">Sign up</Button>
                    </Grid>
                    <PopupMessage message={errorMessage} isSuccess={isSuccess} handleClose={handleErrorPopupClose} open={errorPopupOpen}/>
                </Grid>
            </form>
        </>
    );
}