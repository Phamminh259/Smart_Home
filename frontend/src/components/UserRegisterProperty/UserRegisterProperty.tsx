// @ts-nocheck
import {UserService} from "../../services/UserService.ts";
import {
    Button,
    CssBaseline, FormControl,
    Grid, InputLabel, MenuItem, Select, SelectChangeEvent, Stack, TextField, Typography
} from "@mui/material";
import {SideNav} from "../Sidenav/SideNav.tsx";
import React, {useEffect, useState} from "react";
import {useForm} from "react-hook-form";
import {Property} from"../../models/Property"
import {PropertyService} from "../../services/PropertyService";
import {PopupMessage} from "../PopupMessage/PopupMessage";
import {PropertyType} from "../../models/enums/PropertyType";

type PropertyForm = {
    address: string,
    city: string,
    propertyType: PropertyType,
    size: string,
    floors: string,
}

interface PropertyProps {
    userService: UserService,
    propertyService: PropertyService
}

export function UserRegisterProperty({userService, propertyService} : PropertyProps) {
    const [selectedImage, setSelectedImage] = useState(new File([], "init"));
    const defaultPictureUrl = "https://t3.ftcdn.net/jpg/05/11/52/90/360_F_511529094_PISGWTmlfmBu1g4nocqdVKaHBnzMDWrN.jpg"
    const [imageUrl, setImageUrl] = useState(defaultPictureUrl);
    const [errorMessage, setErrorMessage] = React.useState('');
    const [errorPopupOpen, setErrorPopupOpen] = React.useState(false);
    const [isSuccess, setIsSuccess] = React.useState(true);

    const {register, handleSubmit, formState: {errors}} = useForm<PropertyForm>({
        defaultValues: {
            address: "",
            city: "Novi Sad",
            propertyType: "House",
            size: "",
            floors: ""
        },
        mode: "onChange"
    });

    const onSubmit = (formData : PropertyForm) => registerProperty(formData)
    useEffect(() => {
        if (selectedImage && selectedImage.name != 'init') {
            setImageUrl(URL.createObjectURL(selectedImage));
        }
    }, [selectedImage]);

    function registerProperty(formData : PropertyForm) {
        if (imageUrl == defaultPictureUrl) {
            setErrorMessage("Please select a property picture!");
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
        const newProperty: Property = {
            address: formData.address.trim(),
            city: cityValue,
            size: formData.size.trim(),
            floors: formData.floors.trim(),
            picture: selectedImage,
            owner: sessionStorage.getItem("username"),
            propertyType: type
        };
        console.log(newProperty)
        console.log(propertyService)
        propertyService.registerProperty(newProperty).then((response) => {
            setErrorMessage(response);
            setIsSuccess(true);
            setErrorPopupOpen(true);
        }).catch((error) => {
            console.log(error.response.data)
            setErrorMessage(error.response.data);
            setIsSuccess(false);
            setErrorPopupOpen(true);
        });
    }

    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);
    };

    // const position = [51.505, -0.09];

    const [cityValue, setCity] = React.useState('');
    const handleChangeCity = (event: SelectChangeEvent) => {
        setCity(event.target.value as string);
    };

    const [type, setType] = React.useState('');
    const handleChangeType = (event: SelectChangeEvent) => {
        setType(event.target.value as string);

    };
    return (
        <>
            <CssBaseline/>
            <form onSubmit={handleSubmit(onSubmit)}>
                <Grid container
                      direction={'row'}
                      justifyContent={"center"}>
                <Grid container className={'dark-background'} height={'100%'}  justifyContent={'flex-start'}>
                    <Grid item xs={0} sm={0} md={2} lg={2} xl={2}>
                        <SideNav userService={userService}/>
                    </Grid>
                    <Grid item height={'100%'}  xl={10} lg={10} md={10} sm={12} xs={12}
                          p={2}
                          className={'white-background'}
                          style={{borderRadius:'1.5em', overflowY:'scroll'}}
                          alignItems={'flex-start'}
                          ml={{xl: '20%', lg: '20%', md: '25%', sm: '0', xs: '0'}}
                          mt={{xl: 0, lg: 0, md: 0, sm: '64px', xs: '64px'}}>
                            <Grid container
                                  item
                                  xs={12} sm={12} md={12} lg={12} xl={12}
                                  direction={'row'}
                                  justifyContent={"center"}>
                                <Grid item container rowSpacing={0}>
                                    <Grid item xs={12} sm={12} md={12} lg={12} xl={12}>
                                        <Typography variant="h2" mb={5} fontWeight={400}>Register Property</Typography>
                                    </Grid>
                                    <Grid item container xs={12} sm={12} md={7} lg={7} xl={7} rowSpacing={2}>
                                    <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                                          justifyContent={'center'}>
                                        <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                                            <TextField id="address"
                                                       label="Address"
                                                       fullWidth={true}
                                                       {...register("address",
                                                           {
                                                               required: "Address is a required field!",
                                                           })}
                                                       error={!!errors.address}
                                                       helperText={errors.address ? errors.address?.message : "Required"}
                                                       variant="outlined"/>
                                        </Grid>
                                    </Grid>
                                    <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                                          justifyContent={'center'}>
                                        <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                                            <FormControl fullWidth>
                                                <InputLabel id="demo-simple-select-label">City</InputLabel>
                                                <Select
                                                    labelId="demo-simple-select-label"
                                                    id="demo-simple-select"
                                                    value={cityValue}
                                                    label="City"
                                                    onChange={handleChangeCity}
                                                >
                                                    <MenuItem value={"Novi Sad"}>HA NOI</MenuItem>
                                                    <MenuItem value={"Beograd"}>TP HCM</MenuItem>
                                                    <MenuItem value={"Sombor"}>DA NANG</MenuItem>
                                                </Select>
                                            </FormControl>
                                        </Grid>
                                    </Grid>
                                    <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                                          justifyContent={'center'}>
                                        <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                                            <FormControl fullWidth>
                                                <InputLabel id="demo-simple-select-label">Property Type</InputLabel>
                                                <Select
                                                    labelId="demo-simple-select-label"
                                                    id="demo-simple-select"
                                                    value={type}
                                                    label="Type"
                                                    onChange={handleChangeType}
                                                >
                                                    <MenuItem value={"APARTMENT"}>Apartment</MenuItem>
                                                    <MenuItem value={"HOUSE"}>House</MenuItem>
                                                </Select>
                                            </FormControl>
                                        </Grid>
                                    </Grid>
                                        <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                                              justifyContent={'center'}>
                                            <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                                            <TextField id="size" label="Size"
                                                       fullWidth={true}
                                                       type="size"
                                                       {...register("size",
                                                           {required: "Password is a required field!"})}
                                                       error={!!errors.city}
                                                       helperText={errors.city? errors.city?.message : "Required"}
                                                       variant="outlined"/>
                                        </Grid>
                                    <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                                          justifyContent={'center'}>
                                        <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                                            <TextField id="floors" label="Floors"
                                                       fullWidth={true}
                                                       type="floors"
                                                       {...register("floors",
                                                           {required: "Password is a required field!"})}
                                                       error={!!errors.city}
                                                       helperText={errors.city? errors.city?.message : "Required"}
                                                       variant="outlined"/>
                                        </Grid>
                                    </Grid>
                                    <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                                          justifyContent={'center'}>
                                        <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
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

                                    </Grid>
                                </Grid>
                        <Grid item container xs={12} sm={12} md={5} lg={5} xl={5} alignItems={'center'} justifyContent={'center'}>
                            {/*<MapContainer center={position} zoom={13} scrollWheelZoom={false}>*/}
                            {/*    <TileLayer*/}
                            {/*        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'*/}
                            {/*        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"*/}
                            {/*    />*/}
                            {/*    <Marker position={position}>*/}
                            {/*        <Popup>*/}
                            {/*            A pretty CSS3 popup. <br /> Easily customizable.*/}
                            {/*        </Popup>*/}
                            {/*    </Marker>*/}
                            {/*</MapContainer>*/}
                                </Grid>
                            </Grid>

                            </Grid>
                        <Grid item xs={12} sm={12} md={12} lg={12} xl={12} mt={5}>
                            <Button variant="contained" type="submit">Register Property</Button>
                        </Grid>
                    </Grid>
                </Grid>
                    <PopupMessage message={errorMessage} isSuccess={isSuccess} handleClose={handleErrorPopupClose} open={errorPopupOpen}/>
                </Grid>
            </form>
        </>
    );
}