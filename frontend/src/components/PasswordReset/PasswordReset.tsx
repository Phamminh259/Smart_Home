// @ts-nocheck
import {Button, Grid, TextField, Typography} from "@mui/material";
import {UserService} from "../../services/UserService.ts";
import React from "react";
import {useForm} from "react-hook-form";
import {PopupMessage} from "../PopupMessage/PopupMessage.tsx";
import {NewPassword} from "../../models/NewPassword.ts";

interface PasswordResetProps {
    userService: UserService
}

type PasswordResetForm = {
    password: string,
    passwordConfirmation: string
}

export function PasswordReset({userService} : PasswordResetProps) {

    const [errorMessage, setErrorMessage] = React.useState('');
    const [errorPopupOpen, setErrorPopupOpen] = React.useState(false);
    const [isSuccess, setIsSuccess] = React.useState(true);
    const {register, getValues, handleSubmit, formState: {errors}} = useForm<PasswordResetForm>({
        defaultValues: {
            password: "",
            passwordConfirmation: ""
        },
        mode: "onChange"
    });

    const onSubmit = (formData : PasswordResetForm) => passwordReset(formData)

    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);
    };

    function passwordReset(formData: PasswordResetForm) {
        const userId = String(location.pathname.split('/').pop());
        const newPass : NewPassword = {
            password: formData.password,
            userId: +userId
        }
        userService.passwordReset(newPass).then((response) => {
            setErrorMessage(response);
            setIsSuccess(true);
            setErrorPopupOpen(true);
        }).catch((error) => {
            console.log(error)
            setErrorMessage(error.response.data);
            setIsSuccess(false);
            setErrorPopupOpen(true);
        });
    }

    return (
        <>
            <form onSubmit={handleSubmit(onSubmit)} className={'height-100 overflow'}>
                <Grid container alignItems={'center'}
                      justifyContent={'center'}
                      className='dark-background overflow'
                      height={'100%'}>
                    <Grid container item xs={12} sm={12} md={10} lg={8} xl={8}
                          height={'fit-content'}
                          rowSpacing={3}
                          direction={'column'}
                          justifyContent={"center"}
                          minHeight={'70vh'}
                          className="container rounded-container">
                        <Grid item xs={12} sm={12} md={12} lg={12} xl={12}>
                            <Typography variant="h2" mb={5} fontWeight={400}>Password Reset</Typography>
                        </Grid>
                        <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                              justifyContent={'center'}>
                            <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
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
                        </Grid>
                        <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                              justifyContent={'center'}>
                            <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
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
                        <Grid item mt={4} mb={3}>
                            <Button variant="contained" type="submit">Reset</Button>
                        </Grid>
                        <PopupMessage message={errorMessage} isSuccess={isSuccess} handleClose={handleErrorPopupClose} open={errorPopupOpen}/>
                    </Grid>
                </Grid>
            </form>
        </>
    );
}