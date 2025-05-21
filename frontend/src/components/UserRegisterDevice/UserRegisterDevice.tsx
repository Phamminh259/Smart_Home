// noinspection TypeScriptValidateTypes
// @ts-nocheck
import {UserService} from "../../services/UserService.ts";
import {
    Button,
    Checkbox,
    CssBaseline,
    FormControl,
    FormControlLabel, FormHelperText,
    FormLabel,
    Grid, InputAdornment,
    InputLabel,
    MenuItem,
    OutlinedInput,
    Radio,
    RadioGroup,
    Select,
    SelectChangeEvent,
    Slider,
    Stack,
    TextField,
    Typography
} from "@mui/material";
import {SideNav} from "../Sidenav/SideNav.tsx";
import React, {useEffect, useState} from "react";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router-dom";
import {PopupMessage} from "../PopupMessage/PopupMessage";
import axios from 'axios';
import TagsInput from 'react-tagsinput';
import 'react-tagsinput/react-tagsinput.css';
import {LocalizationProvider, TimeField} from "@mui/x-date-pickers";
import {Dayjs} from "dayjs";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {RoleEnum} from "../../models/enums/RoleEnum";

function isInt(value) {
    return !isNaN(value) &&
        parseInt(Number(value)) == value &&
        !isNaN(parseInt(value, 10));
}

interface UserMainProps {
    userService: UserService,
}

interface DeviceForm {
    name: string,
    type: string,
    propertyId: number,
    energySource: string,
    energyExpenditure?: number,
    measuringUnit?: string,
    picture: string,
    minTemperature?: number,
    maxTemperature?: number,
    fanSpeed?: number,
}

export function UserRegisterDevice({userService}: UserMainProps) {
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = React.useState<string>("");
    const [errorPopupOpen, setErrorPopupOpen] = React.useState<boolean>(false);
    const [isSuccess, setIsSuccess] = React.useState(true);

    let propertyId = Number(location.pathname.split('/').pop());
    if (!propertyId) {
        propertyId = 0;
    }

    const {register, handleSubmit, setValue, formState: {errors}} = useForm<DeviceForm>({
        defaultValues: {
            name: "",
            type: "thermometer",
            propertyId: propertyId,
            energySource: "AUTONOMOUS",
            energyExpenditure: 2,
            measuringUnit: "CELSIUS",
            picture: "",
            minTemperature: 14,
            maxTemperature: 34,
            fanSpeed: 1,
        },
        mode: "onChange"
    });

    const [startTime, setStartTime] = React.useState<Dayjs | null>(null);
    const [endTime, setEndTime] = React.useState<Dayjs | null>(null);

    const [gateMode, setGateMode] = React.useState('public');
    const [gatePlates, setGatePlates] = React.useState([]);

    const gatePlatesChanged = (plates) => {
        setGatePlates(plates);
    }

    const gateModeChanged = (event: React.ChangeEvent<HTMLInputElement>) => {
        setGateMode(event.target.value);
        setValue('gateMode', event.target.value);
    }

    const [energySource, setEnergySource] = React.useState('autonomous');
    const [measuringUnit, setMeasuringUnit] = React.useState('celsius');

    const handleEnergySourceChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setEnergySource(event.target.value);
        setValue('energySource', event.target.value);
    };

    const handleMeasuringUnitChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setMeasuringUnit(event.target.value);
        setValue('measuringUnit', event.target.value);
    };

    useEffect(() => {
        if (sessionStorage.getItem("expiration") != null) {
            setTimeout(() => {
                setErrorMessage("Session expired. Please log in again.");
                setIsSuccess(false);
                setErrorPopupOpen(true);
                setTimeout(() => navigate("/"), 5000);
            }, Number(sessionStorage.getItem("expiration")) - Date.now())
        } else {
            navigate("/")
        }
    });

    const onSubmit = async (formData: DeviceForm) => {
        async function submitGateRegistration(deviceFormData: FormData) {
            if (gateMode === 'private') {
                deviceFormData.append('publicMode', 'false');
            } else {
                deviceFormData.append('publicMode', 'true');
            }

            deviceFormData.append('allowedRegistrationPlates', gatePlates.join(','));

            await axios.post('http://localhost:8080/api/devices/registerGate', deviceFormData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': 'Bearer ' + sessionStorage.getItem('user')
                },
            });

            return true;
        }

        async function submitLampRegistration(deviceFormData: FormData) {
            await axios.post('http://localhost:8080/api/devices/registerLamp', deviceFormData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': 'Bearer ' + sessionStorage.getItem('user')
                },
            });

            return true;
        }

        async function submitChargerRegistration(deviceFormData: FormData) {
            if (!Number(chargerPower)) {
                setErrorMessage('Power must be a number!');
                setErrorPopupOpen(true);
                setIsSuccess(false);
                return false;
            }

            deviceFormData.append('power', chargerPower);
            deviceFormData.append('numberOfPorts', numberOfPorts);
            deviceFormData.append('chargeUntil', chargeUntil / 100.0);

            await axios.post('http://localhost:8080/api/devices/registerCharger', deviceFormData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': 'Bearer ' + sessionStorage.getItem('user')
                },
            });

            return true;
        }

        async function submitSolarPanelSystemRegistration(deviceFormData: FormData) {
            if (!isInt(numberOfPanels) || parseInt(numberOfPanels) <= 0) {
                setErrorMessage('Number of panels must be a positive whole number!');
                setErrorPopupOpen(true);
                setIsSuccess(false);
                return false;
            }

            if (!Number(panelSize) || parseFloat(panelSize) <= 0) {
                setErrorMessage('Panel size must be a positive number!');
                setErrorPopupOpen(true);
                setIsSuccess(false);
                return false;
            }

            deviceFormData.append('numberOfPanels', numberOfPanels);
            deviceFormData.append('panelSize', panelSize);
            deviceFormData.append('panelEfficiency', panelEfficiency / 100.0);

            await axios.post('http://localhost:8080/api/devices/registerSolarPanelSystem', deviceFormData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': 'Bearer ' + sessionStorage.getItem('user')
                },
            });

            return true;
        }

        async function submitSprinklerSystemRegistration(deviceFormData: FormData) {
            if (automaticMode && (!startTime || !endTime || !startTime.isValid() || !endTime.isValid())) {
                setErrorMessage('Start and end time must be set if automatic mode is on!');
                setErrorPopupOpen(true);
                setIsSuccess(false);
                return false;
            }

            deviceFormData.append('specialMode', automaticMode);

            if (automaticMode) {
                deviceFormData.append('startTime', startTime?.format('HH:mm:ss'));
                deviceFormData.append('endTime', endTime?.format('HH:mm:ss'));
            }

            await axios.post('http://localhost:8080/api/devices/registerSprinklerSystem', deviceFormData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': 'Bearer ' + sessionStorage.getItem('user')
                },
            });

            return true;
        }

        async function submitAirConditionerRegistration(deviceFormData: FormData) {
            if (formData.minTemperature && formData.maxTemperature && parseInt(formData.minTemperature) > parseInt(formData.maxTemperature)) {
                setErrorMessage('Min temperature cannot be greater than max temperature!');
                setErrorPopupOpen(true);
                setIsSuccess(false);
                return false;
            }

            if (formData.measuringUnit?.toUpperCase() === 'CELSIUS') {
                if (formData.minTemperature && parseInt(formData.minTemperature) < 14) {
                    setErrorMessage('Min temperature cannot be less than 14 degrees Celsius!');
                    setErrorPopupOpen(true);
                    setIsSuccess(false);
                    return false;
                }
                if (formData.maxTemperature && parseInt(formData.maxTemperature) > 38) {
                    setErrorMessage('Max temperature cannot be greater than 38 degrees Celsius!');
                    setErrorPopupOpen(true);
                    setIsSuccess(false);
                    return false;
                }
            }

            if (formData.measuringUnit?.toUpperCase() === 'FAHRENHEIT') {
                if (formData.minTemperature && parseInt(formData.minTemperature) < 58) {
                    setErrorMessage('Min temperature cannot be less than 58 degrees Fahrenheit!');
                    setErrorPopupOpen(true);
                    setIsSuccess(false);
                    return false;
                }
                if (formData.maxTemperature && parseInt(formData.maxTemperature) > 101) {
                    setErrorMessage('Max temperature cannot be greater than 101 degrees Fahrenheit!');
                    setErrorPopupOpen(true);
                    setIsSuccess(false);
                    return false;
                }
            }

            if (formData.fanSpeed && formData.fanSpeed < 1) {
                setErrorMessage('Number of fan speeds must be greater than 0!');
                setErrorPopupOpen(true);
                setIsSuccess(false);
                return false;
            }

            if (!heating && !cooling && !dry && !auto) {
                setErrorMessage('At least one mode (out of heating, cooling, ventilation & automatic) must be selected!');
                setErrorPopupOpen(true);
                setIsSuccess(false);
                return false;
            }

            deviceFormData.append('fanSpeed', formData.fanSpeed);
            deviceFormData.append('temperatureUnit', formData.measuringUnit?.toUpperCase());
            deviceFormData.append('minTemperature', formData.minTemperature?.toString() || '');
            deviceFormData.append('maxTemperature', formData.maxTemperature?.toString() || '');
            deviceFormData.append('cooling', cooling);
            deviceFormData.append('heating', heating);
            deviceFormData.append('dry', dry);
            deviceFormData.append('auto', auto);
            deviceFormData.append('health', health);
            deviceFormData.append('fungusPrevention', fungusPrevention);

            await axios.post('http://localhost:8080/api/devices/registerAirConditioner', deviceFormData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': 'Bearer ' + sessionStorage.getItem('user')
                },
            });
            return true;
        }

        async function submitWashingMachineRegistration(deviceFormData: FormData) {
            if (formData.minTemperature && formData.maxTemperature && parseInt(formData.minTemperature) > parseInt(formData.maxTemperature)) {
                setErrorMessage('Min temperature cannot be greater than max temperature!');
                setErrorPopupOpen(true);
                setIsSuccess(false);
                return false;
            }

            if (formData.measuringUnit?.toUpperCase() === 'CELSIUS') {
                if (formData.minTemperature && parseInt(formData.minTemperature) < 30) {
                    setErrorMessage('Min temperature cannot be less than 30 degrees Celsius!');
                    setErrorPopupOpen(true);
                    setIsSuccess(false);
                    return false;
                }
                if (formData.maxTemperature && parseInt(formData.maxTemperature) > 95) {
                    setErrorMessage('Max temperature cannot be greater than 95 degrees Celsius!');
                    setErrorPopupOpen(true);
                    setIsSuccess(false);
                    return false;
                }
            }
            if (formData.measuringUnit?.toUpperCase() === 'FAHRENHEIT') {
                if (formData.minTemperature && parseInt(formData.minTemperature) < 86) {
                    setErrorMessage('Min temperature cannot be less than 86 degrees Fahrenheit!');
                    setErrorPopupOpen(true);
                    setIsSuccess(false);
                    return false;
                }
                if (formData.maxTemperature && parseInt(formData.maxTemperature) > 203) {
                    setErrorMessage('Max temperature cannot be greater than 203 degrees Fahrenheit!');
                    setErrorPopupOpen(true);
                    setIsSuccess(false);
                    return false;
                }
            }

            deviceFormData.append('centrifugeMin', centrifuge[0]);
            deviceFormData.append('centrifugeMax', centrifuge[1]);
            deviceFormData.append('temperatureUnit', formData.measuringUnit?.toUpperCase());
            deviceFormData.append('temperatureMin', formData.minTemperature?.toString() || '');
            deviceFormData.append('temperatureMax', formData.maxTemperature?.toString() || '');
            deviceFormData.append('cottons', cottons);
            deviceFormData.append('synthetics', synthetics);
            deviceFormData.append('dailyExpress', dailyExpress);
            deviceFormData.append('wool', wool);
            deviceFormData.append('darkWash', darkWash);
            deviceFormData.append('outdoor', outdoor);
            deviceFormData.append('shirts', shirts);
            deviceFormData.append('duvet', duvet);
            deviceFormData.append('mixed', mixed);
            deviceFormData.append('steam', steam);
            deviceFormData.append('rinseAndSpin', rinseAndSpin);
            deviceFormData.append('spinOnly', spinOnly);
            deviceFormData.append('hygiene', hygiene);

            await axios.post('http://localhost:8080/api/devices/registerWashingMachine', deviceFormData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': 'Bearer ' + sessionStorage.getItem('user')
                },
            });

            return true;
        }

        async function submitThermometerRegistration(deviceFormData: FormData) {
            deviceFormData.append('temperatureUnit', formData.measuringUnit?.toUpperCase());

            await axios.post('http://localhost:8080/api/devices/registerThermometer', deviceFormData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': 'Bearer ' + sessionStorage.getItem('user')
                },
            });
            return true;
        }

        async function submitBatteryRegistration(deviceFormData: FormData) {
            if (!Number(batteryCapacity)) {
                setErrorMessage('Battery capacity must be a number!');
                setErrorPopupOpen(true);
                setIsSuccess(false);
                return false;
            }

            deviceFormData.append('capacity', batteryCapacity);

            await axios.post('http://localhost:8080/api/devices/registerBattery', deviceFormData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': 'Bearer ' + sessionStorage.getItem('user')
                },
            });
            return true;
        }

        function initDeviceFormData() {
            const deviceFormData = new FormData();
            deviceFormData.append('name', formData.name);
            deviceFormData.append('propertyId', formData.propertyId.toString());
            deviceFormData.append('powerSource', formData.energySource.toUpperCase());
            deviceFormData.append('energyConsumption', formData.energyExpenditure?.toString() || '');
            deviceFormData.append('image', selectedImage);

            if (selectedImage.name == 'init') {
                deviceFormData.delete('image');
            }
            return deviceFormData;
        }

        try {
            const deviceFormData = initDeviceFormData();

            // check if energy expenditure is a number, otherwise return error
            if (formData.energyExpenditure && isNaN(formData.energyExpenditure)) {
                setErrorMessage('Energy expenditure must be a number!');
                setErrorPopupOpen(true);
                setIsSuccess(false);
                return;
            }

            if (deviceType == 'thermometer') {
                if (!await submitThermometerRegistration(deviceFormData)) return;
            } else if (deviceType == 'airConditioner') {
                if (!await submitAirConditionerRegistration(deviceFormData)) return;
            } else if (deviceType == 'washingMachine') {
                if (!await submitWashingMachineRegistration(deviceFormData)) return;
            } else if (deviceType == "solarPanelSystem") {
                if (!await submitSolarPanelSystemRegistration(deviceFormData)) return;
            } else if (deviceType == "battery") {
                if (!await submitBatteryRegistration(deviceFormData)) return;
            } else if (deviceType == "charger") {
                if (!await submitChargerRegistration(deviceFormData)) return;
            } else if (deviceType == "lamp") {
                if (!await submitLampRegistration(deviceFormData)) return;
            } else if (deviceType == "gate") {
                if (!await submitGateRegistration(deviceFormData)) return;
            } else if (deviceType == "sprinkler") {
                if (!await submitSprinklerSystemRegistration(deviceFormData)) return;
            }

            setErrorMessage('Device registered successfully!');
            setErrorPopupOpen(true);
            setIsSuccess(true);
            setTimeout(() => {
                navigate('/devices');
            }, 1000);
        } catch (error) {
            console.log(error);
            if (error.code == "ERR_NETWORK") {
                setErrorMessage('File too large! Please select a file smaller than 5MB!');
            } else if (error.response.data.includes('image' && 'null')) {
                setErrorMessage('Please select an image!');
            } else {
                setErrorMessage(error.response.data);
            }
            setErrorPopupOpen(true);
            setIsSuccess(false);
        }
    };

    const [selectedImage, setSelectedImage] = useState(new File([], "init"));
    const defaultPictureUrl = "https://t3.ftcdn.net/jpg/05/11/52/90/360_F_511529094_PISGWTmlfmBu1g4nocqdVKaHBnzMDWrN.jpg"
    const [imageUrl, setImageUrl] = useState(defaultPictureUrl);
    useEffect(() => {
        if (selectedImage && selectedImage.name != 'init') {
            setImageUrl(URL.createObjectURL(selectedImage));
        }
    }, [selectedImage]);

    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);
    };

    const [deviceType, setDeviceType] = React.useState('thermometer');

    /* AC checkboxes */
    const [cooling, setCooling] = React.useState(false);
    const [heating, setHeating] = React.useState(false);
    const [dry, setDry] = React.useState(false);
    const [auto, setAuto] = React.useState(false);
    const [health, setHealth] = React.useState(false);
    const [fungusPrevention, setFungusPrevention] = React.useState(false);


    const [automaticMode, setAutomaticMode] = React.useState(false);

    const automaticModeChanged = () => {
        setAutomaticMode(!automaticMode);
    }
    /* Washing machine */

    /* Checkboxes */
    const [cottons, setCottons] = React.useState(false);
    const [synthetics, setSynthetics] = React.useState(false);
    const [dailyExpress, setDailyExpress] = React.useState(false);
    const [wool, setWool] = React.useState(false);
    const [darkWash, setDarkWash] = React.useState(false);
    const [outdoor, setOutdoor] = React.useState(false);
    const [shirts, setShirts] = React.useState(false);
    const [duvet, setDuvet] = React.useState(false);
    const [mixed, setMixed] = React.useState(false);
    const [steam, setSteam] = React.useState(false);
    const [rinseAndSpin, setRinseAndSpin] = React.useState(false);
    const [spinOnly, setSpinOnly] = React.useState(false);
    const [hygiene, setHygiene] = React.useState(false);

    const [centrifuge, setCentrifuge] = React.useState<number[]>([400, 1600]);

    const [panelEfficiency, setPanelEfficiency] = React.useState<number>(20);

    const panelEfficiencyChanged = (event: Event, newValue: number) => {
        setPanelEfficiency(newValue as number);
    }

    const centrifugeChanged = (event: Event, newValue: number | number[]) => {
        setCentrifuge(newValue as number[]);
    };

    const deviceTypeChanged = (event: SelectChangeEvent) => {
        const value = event.target.value as string;
        setDeviceType(value);
        if (value == 'solarPanelSystem' || value == 'battery') {
            setEnergySource('autonomous');
        }
    }

    /* AC checkboxes */
    const coolingChanged = () => {
        setCooling(!cooling);
    };

    const heatingChanged = () => {
        setHeating(!heating);
    };

    const dryChanged = () => {
        setDry(!dry);
    };

    const autoChanged = () => {
        setAuto(!auto);
    };

    const healthChanged = () => {
        setHealth(!health);
    };

    const fungusPreventionChanged = () => {
        setFungusPrevention(!fungusPrevention);
    };

    /* Washing machine checkboxes */
    const cottonsChanged = () => {
        setCottons(!cottons);
    }

    const syntheticsChanged = () => {
        setSynthetics(!synthetics);
    }

    const dailyExpressChanged = () => {
        setDailyExpress(!dailyExpress);
    }

    const woolChanged = () => {
        setWool(!wool);
    }

    const darkWashChanged = () => {
        setDarkWash(!darkWash);
    }

    const outdoorChanged = () => {
        setOutdoor(!outdoor);
    }

    const shirtsChanged = () => {
        setShirts(!shirts);
    }

    const duvetChanged = () => {
        setDuvet(!duvet);
    }

    const mixedChanged = () => {
        setMixed(!mixed);
    }

    const steamChanged = () => {
        setSteam(!steam);
    }

    const rinseAndSpinChanged = () => {
        setRinseAndSpin(!rinseAndSpin);
    }

    const spinOnlyChanged = () => {
        setSpinOnly(!spinOnly);
    }

    const hygieneChanged = () => {
        setHygiene(!hygiene);
    }

    const [numberOfPanels, setNumberOfPanels] = React.useState<number>(0);
    const [panelSize, setPanelSize] = React.useState<number>(0);

    const numberOfPanelsChanged = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = event.target.value as number;
        setNumberOfPanels(newValue);
    }

    const panelSizeChanged = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = event.target.value as number;
        setPanelSize(newValue);
    }

    const [batteryCapacity, setBatteryCapacity] = React.useState<number>(1);

    const batteryCapacityChanged = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = event.target.value as number;
        setBatteryCapacity(newValue);
    }

    const [chargerPower, setChargerPower] = React.useState<number>(1);

    const chargerPowerChanged = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = event.target.value as number;
        setChargerPower(newValue);
    }

    const [numberOfPorts, setNumberOfPorts] = React.useState<number>(1);

    const numberOfPortsChanged = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = event.target.value as number;
        setNumberOfPorts(newValue);
    }

    const [chargeUntil, setChargeUntil] = React.useState<number>(100);

    const chargeUntilChanged = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = event.target.value as number;
        setChargeUntil(newValue);
    }

    function solarPanelSystemForm() {
        return <div>
            <TextField id="numberOfPanels" label="Số lượng tấm pin" fullWidth={true} type={"number"} variant="outlined"
                       margin={"normal"} value={numberOfPanels} onChange={numberOfPanelsChanged}/>
            <FormControl sx={{m: 1, width: '25ch'}} variant="outlined">
                <OutlinedInput id="panelSize" aria-describedby={"panelSize-helper-text"} inputProps={{
                    'aria-label': 'panel size',
                }} endAdornment={<InputAdornment position={"end"}>m&sup2;</InputAdornment>}
                               value={panelSize} onChange={panelSizeChanged}/>
                <FormHelperText id="panelSize-helper-text">Kích thước tấm pin m&sup2;</FormHelperText>
            </FormControl>
            <p style={{color: 'rgba(0, 0, 0, 0.6)'}}>Hiệu suất pin (%):</p><br/>
            <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                  justifyContent={'center'}>
                <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                    <Slider defaultValue={20} getAriaLabel={() => 'Panel efficiency'} valueLabelDisplay="on"
                            min={1} max={100} value={panelEfficiency} onChange={panelEfficiencyChanged}/>
                </Grid>
            </Grid>
        </div>;
    }

    function airConditionerForm() {
        return <div>
            {/*fan speed text field*/}
            <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                  justifyContent={'center'} marginBottom={'20px'}>
                <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                    <TextField id="fanSpeed"
                               label="Tốc độ quạt tối đa"
                               fullWidth={true}
                               type={"number"}
                               {...register("fanSpeed")}
                               variant="outlined"/>
                </Grid>
            </Grid>
            <p style={{color: 'rgba(0, 0, 0, 0.6)'}}>Đơn vị nhiệt độ:</p>
            <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                  justifyContent={'center'} marginBottom={'10px'}>
                <Grid item xs={12} sm={12} md={8} lg={8} xl={6} display="flex" gap={2}>
                    <TextField
                        id="minTemperature"
                        label="Tối thiểu"
                        type="number"
                        InputLabelProps={{
                            shrink: true,
                        }}
                        {...register("minTemperature", {required: "Minimum temperature is a required field!"})}
                        error={!!errors.minTemperature}
                        helperText={errors.minTemperature ? errors.minTemperature?.message : "Required"}
                        fullWidth
                    />

                    <TextField
                        id="maxTemperature"
                        label="Tối đa"
                        type="number"
                        InputLabelProps={{
                            shrink: true,
                        }}
                        {...register("maxTemperature", {required: "Maximum temperature is a required field!"})}
                        error={!!errors.maxTemperature}
                        helperText={errors.maxTemperature ? errors.maxTemperature?.message : "Required"}
                        fullWidth
                    />
                </Grid>
            </Grid>
            <FormControl component="fieldset">
                <FormLabel component="legend">Đơn Vị Nhiệt Độ</FormLabel>
                <RadioGroup
                    aria-label="measuringUnit"
                    value={measuringUnit}
                    onChange={handleMeasuringUnitChange}
                    style={{flexDirection: 'row'}}
                >
                    <FormControlLabel value="celsius" control={<Radio/>} label="Độ C"/>
                    <FormControlLabel value="fahrenheit" control={<Radio/>}
                                      label="Độ F"/>
                </RadioGroup>
            </FormControl>
            <br/><br/>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Làm lạnh</FormLabel>
                <Checkbox aria-label={"cooling"} value={cooling} onChange={coolingChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Làm nóng</FormLabel>
                <Checkbox aria-label={"heating"} value={heating} onChange={heatingChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Thông gió</FormLabel>
                <Checkbox aria-label={"dry"} value={dry} onChange={dryChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Tự Động</FormLabel>
                <Checkbox aria-label={"auto"} value={auto} onChange={autoChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Lọc không khí</FormLabel>
                <Checkbox aria-label={"health"} value={health} onChange={healthChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"}>
                <FormLabel component={"legend"}>Phòng ngừa nấm</FormLabel>
                <Checkbox aria-label={"fungusPrevention"} value={fungusPrevention}
                          onChange={fungusPreventionChanged}></Checkbox>
            </FormControl>
        </div>;
    }

    function chargerForm() {
        return <div>
            <p style={{color: 'rgba(0, 0, 0, 0.6)'}}>Sạc cho tới khi số phần % pin:</p><br/>
            <Grid item container xs={12} sm={12} md={12} lg={12} xl={12} justifyContent={'center'}>
                <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                    <Slider defaultValue={100} getAriaLabel={() => 'Charge until what % of car battery is filled'}
                            min={1} max={100} value={chargeUntil} onChange={chargeUntilChanged}
                            valueLabelDisplay="on"/>
                </Grid>
            </Grid>
            <FormControl sx={{m: 1, width: '25ch'}} variant="outlined">
                <OutlinedInput id="chargerPower" aria-describedby={"chargerPower-helper-text"} inputProps={{
                    'aria-label': 'charger power',
                }} endAdornment={<InputAdornment position={"end"}>kW</InputAdornment>}
                               value={chargerPower} onChange={chargerPowerChanged}/>
                <FormHelperText id="chargerPower-helper-text">Công suất bộ sạc kW</FormHelperText>
            </FormControl>
            <TextField id="numberOfPorts" label="Số lượng cổng sạc" fullWidth={true} type={"number"}
                       variant="outlined" margin={"normal"} value={numberOfPorts} onChange={numberOfPortsChanged}/>
        </div>
    }

    function gateForm() {
        return <div>
            <FormControl component="fieldset">
                <FormLabel component="legend">Initial mode</FormLabel>
                <RadioGroup
                    aria-label="gateMode"
                    value={gateMode}
                    onChange={gateModeChanged}
                    style={{flexDirection: 'row'}}>
                    <FormControlLabel value="public" control={<Radio/>} label="Public"/>
                    <FormControlLabel value="private" control={<Radio/>}
                                      label="Private"/>
                </RadioGroup>
            </FormControl>
            <p>Nhập biển số xe để cho phép đi qua khi chế độ riêng tư được bật.</p>
            <p>Nhập biển số xe bên dưới</p>
            <TagsInput value={gatePlates} onChange={gatePlatesChanged} onlyUnique={true} inputProps={{
                placeholder: 'Add a plate'
            }}/>
        </div>
    }

    function sprinklerForm() {
        return <div>
            <FormControl component={"fieldset"}>
                <FormLabel component={"legend"}>Chế độ tự động</FormLabel>
                <Checkbox aria-label={"automaticMode"} value={automaticMode} onChange={automaticModeChanged}></Checkbox>
            </FormControl>
            <br/>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <TimeField value={startTime} onChange={(newValue) => setStartTime(newValue)}
                format="HH:mm" label="Start time" margin={"normal"} disabled={!automaticMode}/>
                <TimeField value={endTime} onChange={(newValue) => setEndTime(newValue)}
                           format="HH:mm" label="End time" margin={"normal"} disabled={!automaticMode}/>
            </LocalizationProvider>
        </div>
    }

    function washingMachineForm() {
        return <div>
            <p style={{color: 'rgba(0, 0, 0, 0.6)'}}>Độ ly tâm:</p><br/>
            <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                  justifyContent={'center'}>
                <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                    <Slider defaultValue={400} getAriaLabel={() => 'Centrifuge range'} valueLabelDisplay="on"
                            step={200} marks min={400} max={1600} value={centrifuge} onChange={centrifugeChanged}
                            disableSwap/>
                </Grid>
            </Grid>
            <p style={{color: 'rgba(0, 0, 0, 0.6)'}}>Nhiệt Độ Giặt</p>
            <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                  justifyContent={'center'} marginBottom={'10px'}>
                <Grid item xs={12} sm={12} md={8} lg={8} xl={6} display="flex" gap={2}>
                    <TextField
                        id="minTemperature"
                        label="Tối thiểu"
                        type="number"
                        InputLabelProps={{
                            shrink: true,
                        }}
                        {...register("minTemperature", {required: "Minimum temperature is a required field!"})}
                        error={!!errors.minTemperature}
                        helperText={errors.minTemperature ? errors.minTemperature?.message : "Required"}
                        fullWidth
                    />

                    <TextField
                        id="maxTemperature"
                        label="Tối đa"
                        type="number"
                        InputLabelProps={{
                            shrink: true,
                        }}
                        {...register("maxTemperature", {required: "Maximum temperature is a required field!"})}
                        error={!!errors.maxTemperature}
                        helperText={errors.maxTemperature ? errors.maxTemperature?.message : "Required"}
                        fullWidth
                    />
                </Grid>
            </Grid>
            <FormControl component="fieldset">
                <FormLabel component="legend">Đơn vị nhiệt độ</FormLabel>
                <RadioGroup
                    aria-label="measuringUnit"
                    value={measuringUnit}
                    onChange={handleMeasuringUnitChange}
                    style={{flexDirection: 'row'}}
                >
                    <FormControlLabel value="celsius" control={<Radio/>} label="Độ C"/>
                    <FormControlLabel value="fahrenheit" control={<Radio/>}
                                      label="Độ F"/>
                </RadioGroup>
            </FormControl>
            <br/><br/>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Vải Cottons</FormLabel>
                <Checkbox aria-label={"cottons"} value={cottons} onChange={cottonsChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Vải tổng hợp</FormLabel>
                <Checkbox aria-label={"synthetics"} value={synthetics} onChange={syntheticsChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"}>
                <FormLabel component={"legend"}>Giặt nhanh</FormLabel>
                <Checkbox aria-label={"dailyExpress"} value={dailyExpress} onChange={dailyExpressChanged}></Checkbox>
            </FormControl>
            <br/>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Vải Len</FormLabel>
                <Checkbox aria-label={"wool"} value={wool} onChange={woolChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Giặt đồ đen</FormLabel>
                <Checkbox aria-label={"darkWash"} value={darkWash} onChange={darkWashChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"}>
                <FormLabel component={"legend"}>Outdoor</FormLabel>
                <Checkbox aria-label={"outdoor"} value={outdoor} onChange={outdoorChanged}></Checkbox>
            </FormControl>
            <br/>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Shirts</FormLabel>
                <Checkbox aria-label={"shirts"} value={shirts} onChange={shirtsChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Chăn</FormLabel>
                <Checkbox aria-label={"duvet"} value={duvet} onChange={duvetChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"}>
                <FormLabel component={"legend"}>Pha trộn</FormLabel>
                <Checkbox aria-label={"mixed"} value={mixed} onChange={mixedChanged}></Checkbox>
            </FormControl>
            <br/>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Hơi nước</FormLabel>
                <Checkbox aria-label={"steam"} value={steam} onChange={steamChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Giặt và vắt nước</FormLabel>
                <Checkbox aria-label={"rinseAndSpin"} value={rinseAndSpin} onChange={rinseAndSpinChanged}></Checkbox>
            </FormControl>
            <FormControl component={"fieldset"}>
                <FormLabel component={"legend"}>Vắt nước</FormLabel>
                <Checkbox aria-label={"spinOnly"} value={spinOnly} onChange={spinOnlyChanged}></Checkbox>
            </FormControl>
            <br/>
            <FormControl component={"fieldset"} style={{'marginRight': '20px'}}>
                <FormLabel component={"legend"}>Vệ Sinh lồng giặt</FormLabel>
                <Checkbox aria-label={"hygiene"} value={hygiene} onChange={hygieneChanged}></Checkbox>
            </FormControl>
        </div>;
    }

    function thermometerForm() {
        return <FormControl component="fieldset">
            <FormLabel component="legend">Đơn vị đo</FormLabel>
            <RadioGroup
                aria-label="measuringUnit"
                value={measuringUnit}
                onChange={handleMeasuringUnitChange}
                style={{flexDirection: 'row'}}
            >

                <FormControlLabel value="celsius" control={<Radio/>} label="Độ C"/>
                <FormControlLabel value="fahrenheit" control={<Radio/>}
                                  label="Độ F"/>
            </RadioGroup>
        </FormControl>;
    }

    function batteryForm() {
        return <FormControl sx={{m: 1, width: '25ch'}} variant="outlined">
            <OutlinedInput id="batteryCapacity" aria-describedby={"batteryCapacity-helper-text"} inputProps={{
                'aria-label': 'battery capacity',
            }} endAdornment={<InputAdornment position={"end"}>kWh</InputAdornment>}
                           value={batteryCapacity} onChange={batteryCapacityChanged}/>
            <FormHelperText id="batteryCapacity-helper-text">Công suất pin kWh</FormHelperText>
        </FormControl>
    }

    function genericDeviceForm() {
        return <>
            <Grid item container rowSpacing={0}>
                <Grid item container xs={12} sm={12} md={12} lg={12} xl={12} alignItems={'center'}
                      justifyContent={'center'}>
                    <Stack alignItems={'center'} spacing={1}>
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

                <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                      justifyContent={'center'} marginTop={'20px'} marginBottom={'20px'}>
                    <FormControl variant="outlined">
                        <InputLabel id="deviceType-label">Device Type</InputLabel>
                        <Select
                            labelId="deviceType-label"
                            id="deviceType"
                            label="Device Type"
                            defaultValue="thermometer"
                            value={deviceType}
                            onChange={deviceTypeChanged}
                        >
                            <MenuItem value="thermometer">Nhiệt kế cảm biến nhiệt độ</MenuItem>
                            <MenuItem value="airConditioner">Điều hòa</MenuItem>
                            <MenuItem value="washingMachine">Máy Giặt</MenuItem>
                            <MenuItem value="solarPanelSystem">Hệ Thống Pin Năng Lượng Mặt Trời</MenuItem>
                            <MenuItem value="battery">Pin</MenuItem>
                            <MenuItem value="charger">Bộ Sạc</MenuItem>
                            <MenuItem value="lamp">Đèn</MenuItem>
                            <MenuItem value="gate">Cổng ra vào</MenuItem>
                            <MenuItem value="sprinkler">Hệ thống phun nước</MenuItem>
                        </Select>
                    </FormControl>
                </Grid>

                <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                      justifyContent={'center'} marginBottom={'10px'}>
                    <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                        <TextField
                            id="propertyId"
                            label="Property ID"
                            type="number"
                            fullWidth={true}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            {...register("propertyId",
                                {required: "Property ID is a required field!"})}
                            error={!!errors.propertyId}
                            helperText={errors.propertyId ? errors.propertyId?.message : "Required"}/>
                    </Grid>
                </Grid>
                {/*name text field*/}
                <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                      justifyContent={'center'} marginBottom={'10px'}>
                    <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                        <TextField id="name"
                                   label="Name"
                                   fullWidth={true}
                                   {...register("name",
                                       {
                                           required: "Name is a required field!",
                                       })}
                                   error={!!errors.name}
                                   helperText={errors.name ? errors.name?.message : "Required"}
                                   variant="outlined"/>
                    </Grid>
                </Grid>

                <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                      justifyContent={'center'}>
                    <Grid item xs={12} sm={12} md={8} lg={8} xl={6}>
                        <TextField id="energyExpenditure"
                                   label="Tiêu Thụ Năng Lượng (kWh)"
                                   type="text"
                                   fullWidth={true}
                                   {...register("energyExpenditure",
                                       {
                                           required: "Energy expenditure is a required field!",
                                       })}
                                   error={!!errors.energyExpenditure}
                                   helperText={errors.energyExpenditure ? errors.energyExpenditure?.message : "Required"}
                                   disabled={energySource === 'autonomous'}
                                   variant="outlined"/>
                    </Grid>
                </Grid>
            </Grid>
            <Grid item container justifyContent={'center'}>
                <Grid item container xs={12} sm={12} md={8} lg={8} xl={6}
                      style={{display: 'flex', justifyContent: 'center'}}>
                    <FormControl component="fieldset">
                        <FormLabel component="legend">Nguồn Năng Lượng</FormLabel>
                        <RadioGroup
                            aria-label="energySource"
                            value={energySource}
                            onChange={handleEnergySourceChange}
                            style={{flexDirection: 'row'}}
                        >
                            <FormControlLabel value="autonomous" control={<Radio/>}
                                              label="Tự chủ"/>
                            <FormControlLabel value="house" control={<Radio/>} label="Trong Nhà"
                                              disabled={deviceType == 'solarPanelSystem' || deviceType == 'battery'}/>
                        </RadioGroup>
                    </FormControl>
                </Grid>
            </Grid>
        </>;
    }

    return (
        <>
            <CssBaseline/>
            <form onSubmit={handleSubmit(onSubmit)}>
                <Grid container
                      direction={'row'}
                      justifyContent={"center"}>
                    <Grid container className={'dark-background'} height={'100%'} justifyContent={'flex-start'}>
                        <Grid item xs={0} sm={0} md={2} lg={2} xl={2}>
                            <SideNav userService={userService} isAdmin={sessionStorage.getItem("role") == RoleEnum.ROLE_ADMIN ||
                                sessionStorage.getItem("role") == RoleEnum.ROLE_SUPERADMIN} isSuperadmin={sessionStorage.getItem("role") == RoleEnum.ROLE_SUPERADMIN}/>
                        </Grid>
                        <Grid item height={'100%'} xl={10} lg={10} md={10} sm={12} xs={12} p={2}
                              className={'white-background'} style={{
                            borderRadius: '1.5em',
                            overflowY: 'auto',
                            maxHeight: '100vh',
                        }}
                              alignItems={'flex-start'}
                              ml={{xl: '20%', lg: '20%', md: '25%', sm: '0', xs: '0'}}
                              mt={{xl: 0, lg: 0, md: 0, sm: '64px', xs: '64px'}}>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={12}>
                                <Typography variant="h2" mb={5} fontWeight={400}>Register Device</Typography>
                            </Grid>
                            <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}
                                  direction={'row'} justifyContent={"center"} marginBottom={"-2vh"}>
                                <Grid container item xs={12} md={8} lg={8} xl={6}>
                                    <Grid item xs={12} container direction={"column"} spacing={2}>
                                        {/* Left section */}
                                        {genericDeviceForm()}
                                    </Grid>
                                </Grid>
                                <Grid container item xs={12} md={4} lg={4} xl={6}>
                                    <Grid item xs={12} container direction="column" spacing={2}>
                                        {/* Right section */}
                                        <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}
                                              justifyContent={'center'}>
                                            <Grid item container xs={12} sm={12} md={8} lg={8} xl={6}
                                                  style={{
                                                      display: 'flex',
                                                      justifyContent: 'center',
                                                      alignItems: 'center'
                                                  }}>
                                                {deviceType === "thermometer" && thermometerForm()}
                                                {deviceType === "airConditioner" && airConditionerForm()}
                                                {deviceType === "washingMachine" && washingMachineForm()}
                                                {deviceType === "solarPanelSystem" && solarPanelSystemForm()}
                                                {deviceType === "battery" && batteryForm()}
                                                {deviceType === "charger" && chargerForm()}
                                                {deviceType === "gate" && gateForm()}
                                                {deviceType === "sprinkler" && sprinklerForm()}
                                            </Grid>
                                        </Grid>
                                    </Grid>
                                </Grid>
                            </Grid>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={12} mt={5}>
                                <Button variant="contained" type="submit">Register device</Button>
                            </Grid>
                            <PopupMessage message={errorMessage} isSuccess={isSuccess}
                                          handleClose={handleErrorPopupClose} open={errorPopupOpen}/>
                        </Grid>
                    </Grid>
                </Grid>
            </form>
        </>
    );
}