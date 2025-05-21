// @ts-nocheck
import React from 'react';
import {
    Box,
    Button,
    Dialog, DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Grid,
    Tab,
    Tabs,
    TextField,
    Typography
} from "@mui/material";
import {UserCredentials} from "../../models/UserCredentials";
import {Link, useNavigate} from "react-router-dom";
import {Registration} from "../Registration/Registration";
import {PopupMessage} from "../PopupMessage/PopupMessage";
import {useForm} from "react-hook-form";
import './Login.css'
import {UserService} from "../../services/UserService";
import {RoleEnum} from "../../models/enums/RoleEnum.ts";
import {PasswordResetDto} from "../../models/PasswordResetDto.ts";

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

interface LoginProps {
    userService: UserService
}

interface LoginForm {
    username: string,
    password: string
}

interface PasswordResetForm {
    email: string
}

function TabPanel(props: TabPanelProps) {
    const {children, value, index, ...other} = props;
    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`tabpanel-${index}`}
            className="height-100"
            aria-labelledby={`tab-${index}`}
            {...other}>
            {value === index && (
                <Box sx={{p: 3}} component={'div'}>
                    <div>{children}</div>
                </Box>
            )}
        </div>
    );
}

function tabSetup(index: number) {
    return {
        id: `tab-${index}`,
        'aria-controls': `tabpanel-${index}`,
    };
}

export function Login({userService} : LoginProps) {
    const [tabValue, setTabTabValue] = React.useState<number>(0);
    const [errorMessage, setErrorMessage] = React.useState<string>("");
    const [errorPopupOpen, setErrorPopupOpen] = React.useState<boolean>(false);
    const navigate = useNavigate();
    const [isSuccess, setIsSuccess] = React.useState(true);
    const {register, handleSubmit, formState: {errors}} = useForm<LoginForm>({
        defaultValues: {
            username: "",
            password: "",
        },
        mode: "onChange"
    });

    const {register: passwordReset, handleSubmit: handlePasswordResetSubmit, formState: {errors: passwordResetErrors},  setValue} = useForm<PasswordResetForm>({
        defaultValues: {
            email: ""
        },
        mode: "onChange"
    });

    const [openEmailDialog, setOpenEmailDialog] = React.useState(false);

    const handlePasswordResetClickOpen = () => {
        setOpenEmailDialog(true);
    };

    const handlePasswordResetClickClose = () => {
        setOpenEmailDialog(false);
        setValue("email","");
    };
    const handleTabChange = (_event: React.SyntheticEvent,newValue: number) => setTabTabValue(newValue);
    const onSubmit = (formData: LoginForm) => tryLogin(formData)
    const onSubmitEmail = (formData: PasswordResetForm) => resetPassword(formData)

    function tryLogin(formData : LoginForm) {
        const userCredentials: UserCredentials = {
            username: formData.username.trim(),
            password: formData.password.trim()
        };
        userService.loginUser(userCredentials).then(() => {
            const role = sessionStorage.getItem("role");
            if(role == RoleEnum.ROLE_USER)
                navigate('/userMain');
            else if (role == RoleEnum.ROLE_ADMIN)
                navigate("/adminMain");
            else {
                if(sessionStorage.getItem("user") == "null")
                    navigate("/passwordReset/1")
                else
                    navigate("/adminMain");
            }
        }).catch((error) => {
            // console.log(error);
            if (error.response.status == 404) {
                setErrorMessage("Username or password is incorrect");
            } else {
                setErrorMessage(error.response.data);
            }
            setIsSuccess(false);
            setErrorPopupOpen(true);
        });
    }

    function resetPassword(formData: PasswordResetForm) {
        const passwordResetMail : PasswordResetDto = {
            email: formData.email
        }
        userService.sendPasswordResetMail(passwordResetMail).then((response) => {
            setErrorMessage(response);
            setIsSuccess(true);
            setErrorPopupOpen(true);
        }).catch((error) => {
            setErrorMessage(error.response.data);
            setIsSuccess(false);
            setErrorPopupOpen(true);
        });
    }

    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);
    };

    return (
        <Grid container alignItems={'center'}
              justifyContent={'center'}
              className='dark-background overflow'
              height={'100%'}>
            <Grid container item xs={12} sm={12} md={10} lg={8} xl={8}
                  height={'fit-content'}
                  minHeight={'70vh'}
                  className="container rounded-container">
                <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}>
                    <Grid item xs={12} sm={12} md={12} lg={12} xl={12}>
                        <Box sx={{borderBottom: 1, borderColor: 'divider'}} component={'div'}>
                            <Tabs value={tabValue} onChange={handleTabChange} aria-label="login & sign-up" centered>
                                <Tab label="Login" {...tabSetup(0)} />
                                <Tab label="Sign-up" {...tabSetup(1)} />
                            </Tabs>
                        </Box>
                    </Grid>
                    <Grid item justifyContent={'center'} xs={12} sm={12} md={12} lg={12} xl={12} >
                        <TabPanel value={tabValue} index={0}>
                            <form onSubmit={handleSubmit(onSubmit)}>
                                <Grid container
                                      item
                                      xs={12} sm={12} md={12} lg={12} xl={12}
                                      direction={'row'}
                                      justifyContent={"center"}>
                                    <Grid item container rowSpacing={3}>
                                        <Grid item xs={12} sm={12} md={12} lg={12} xl={12}>
                                            <Typography variant="h2" mb={5} fontWeight={400}>Login</Typography>
                                        </Grid>
                                        <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                                              justifyContent={'center'}>
                                            <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                                                <TextField id="username"
                                                           label="Username"
                                                           fullWidth={true}
                                                           {...register("username",
                                                               {
                                                                   required: "Username is a required field!",
                                                               })}
                                                           error={!!errors.username}
                                                           helperText={errors.username ? errors.username?.message : "Required"}
                                                           variant="outlined"/>
                                            </Grid>
                                        </Grid>
                                        <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                                              justifyContent={'center'}>
                                            <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                                                <TextField id="password" label="Password"
                                                           fullWidth={true}
                                                           type="password"
                                                           {...register("password",
                                                               {required: "Password is a required field!"})}
                                                           error={!!errors.password}
                                                           helperText={errors.password? errors.password?.message : "Required"}
                                                           variant="outlined"/>
                                            </Grid>
                                        </Grid>
                                        <Grid item container xs={12} sm={12} md={12} lg={12} xl={12} justifyContent={'center'}>
                                            <Link to={"#"} style={{fontSize:'medium'}} onClick={handlePasswordResetClickOpen}>Forgot password?</Link>
                                        </Grid>
                                        <Grid item xs={12} sm={12} md={12} lg={12} xl={12} mt={5}>
                                            <Button variant="contained" type="submit">Login</Button>
                                        </Grid>
                                    </Grid>
                                </Grid>
                            </form>
                        </TabPanel>
                    </Grid>
                </Grid>
                <Grid item justifyContent={'center'} xs={12} sm={12} md={12} lg={12} xl={12}>
                    <TabPanel value={tabValue} index={1}>
                        <Registration userService={userService} adminRegistration={false}/>
                    </TabPanel>
                </Grid>
                <PopupMessage message={errorMessage} isSuccess={isSuccess} handleClose={handleErrorPopupClose} open={errorPopupOpen}/>
                <React.Fragment>
                    <Dialog open={openEmailDialog} onClose={handlePasswordResetClickClose}>
                        <DialogTitle>Password Reset</DialogTitle>
                        <form onSubmit={handlePasswordResetSubmit(onSubmitEmail)}>
                            <DialogContent>
                                <DialogContentText marginBottom={'1em'}>
                                    To receive password reset email, please enter your email address.
                                </DialogContentText>
                                    <TextField
                                        autoFocus
                                        margin="dense"
                                        id="email"
                                        label="Email Address"
                                        type="email"
                                        fullWidth
                                        {...passwordReset("email",
                                            {
                                                required: "Email is a required field!",
                                            })}
                                        error={!!passwordResetErrors.email}
                                        helperText={passwordResetErrors.email ? passwordResetErrors.email?.message : "Required"}
                                        variant="outlined"
                                    />
                            </DialogContent>
                            <DialogActions>
                                <Button variant="contained" onClick={handlePasswordResetClickClose}>Cancel</Button>
                                <Button variant="contained" type="submit">Send email</Button>
                            </DialogActions>
                        </form>
                    </Dialog>
                </React.Fragment>
            </Grid>
        </Grid>
    );
}