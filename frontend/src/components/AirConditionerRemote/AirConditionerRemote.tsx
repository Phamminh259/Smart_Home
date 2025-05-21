// @ts-nocheck
import {
    Button,
    Checkbox, CircularProgress,
    Dialog,
    DialogContent,
    DialogTitle,
    FormControlLabel,
    Grid,
    IconButton,
    Slider,
    Stack,
    styled,
    Switch,
    ToggleButton,
    ToggleButtonGroup,
    Typography
} from "@mui/material";
import React, {useEffect, useRef} from "react";
import LightModeIcon from "@mui/icons-material/LightMode";
import AcUnitIcon from "@mui/icons-material/AcUnit";
import AirIcon from "@mui/icons-material/Air";
import HdrAutoIcon from "@mui/icons-material/HdrAuto";
import {LocalizationProvider, MobileTimePicker} from "@mui/x-date-pickers";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import CloseIcon from "@mui/icons-material/Close";
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import Stomp, {Client, Message} from "stompjs";
import SockJS from "sockjs-client";
import {Dayjs} from "dayjs";
import {ACValueDigest} from "../../models/ACValueDigest.ts";
import {CommandType} from "../../models/enums/CommandType.ts";
import {ACMode} from "../../models/enums/ACMode.ts";
import {PopupMessage} from "../PopupMessage/PopupMessage.tsx";
import {DeviceCapabilities} from "../../models/DeviceCapabilities.ts";
import {CommandParams} from "../../models/CommandParams.ts";
import {Command} from "../../models/Command.ts";
import {DataGrid, GridColDef, GridValueGetterParams} from "@mui/x-data-grid";
import {Scheduled} from "../../models/Scheduled.ts";
import CheckIcon from '@mui/icons-material/Check';
interface AirConditionerRemoteProps {
    open: boolean,
    handleClose: () => void,
    deviceId: number,
    openSocket: boolean,
}

const AntSwitch = styled(Switch)(({ theme }) => ({
    width: 70,
    height: 25,
    padding: 0,
    display: 'flex',
    '&:active': {
        '& .MuiSwitch-thumb': {
            width: 25,
        },
        '& .MuiSwitch-switchBase.Mui-checked': {
            transform: 'translateX(30px)',
        },
    },
    '& .MuiSwitch-switchBase': {
        padding: 1,
        '&.Mui-checked': {
            transform: 'translateX(45px)',
            color: '#fff',
            '& + .MuiSwitch-track': {
                opacity: 1,
                backgroundColor: theme.palette.mode === 'dark' ? '#177ddc' : '#1890ff',
            },
        },
    },
    '& .MuiSwitch-thumb': {
        boxShadow: '0 2px 4px 0 rgb(0 35 11 / 20%)',
        width: 22,
        height: 22,
        borderRadius: 20,
        transition: theme.transitions.create(['width'], {
            duration: 200,
        }),
    },
    '& .MuiSwitch-track': {
        borderRadius: 24 / 2,
        opacity: 1,
        backgroundColor:
            theme.palette.mode === 'dark' ? 'rgba(255,255,255,.35)' : 'rgba(0,0,0,.25)',
        boxSizing: 'border-box',
    },
}));

interface Mark {
    value: number,
    label: string
}

function getFromDate(params: GridValueGetterParams) {
    return `${new Date(params.row.from).toLocaleString()}`;
}

function getToDate(params: GridValueGetterParams) {
    return `${new Date(params.row.to).toLocaleString()}`;
}

const columnsScheduled: GridColDef[] = [
    { field: 'from',
        headerName: 'From',
        type:'Date',
        width:190,
        valueGetter: getFromDate,
        align:'center',
        headerAlign:'center',
    },
    { field: 'to',
        headerName: 'To',
        type:'Date',
        width:190,
        valueGetter: getToDate,
        align:'center',
        headerAlign:'center',
    },
    { field: 'everyDay',
        headerName: 'Daily',
        type:'boolean',
        align:'center',
        headerAlign:'center' }
];
export function AirConditionerRemote ({open,handleClose, deviceId, openSocket} : AirConditionerRemoteProps)  {

    const [errorMessage, setErrorMessage] = React.useState<string>("");
    const [errorPopupOpen, setErrorPopupOpen] = React.useState<boolean>(false);
    const [isSuccess, setIsSuccess] = React.useState(true);
    const [healthChecked, setHealthChecked] = React.useState(false);
    const [fungusChecked, setFungusChecked] = React.useState(false);
    const [scheduledChecked, setScheduledChecked] = React.useState(false);
    const [repeatChecked, setRepeatChecked] = React.useState(false);
    const [mode, setMode] = React.useState<string | null>("AUTO");
    const [fanSpeedDisable, setFanSpeedDisable] = React.useState<boolean>(false);
    const [fanSpeed, setFanSpeed] = React.useState<number>(2);
    const [targetTemp, setTargetTemp] = React.useState<number>(25);
    const realtimeUrl = "http://localhost:8080/realtime";
    const client = useRef<Client | null>(null);
    const [currentStatus, setCurrentStatus] = React.useState<string>("Offline");
    const [currentStatusColor, setCurrentStatusColor] = React.useState<string>('grey');
    const [isOn, setIsOn] = React.useState<boolean>(false);
    const firstLoad = useRef(true);
    const lastStatusReceived = useRef(0);
    const [from, setFrom] = React.useState<Dayjs | null>(null);
    const [to, setTo] = React.useState<Dayjs | null>(null);
    const [disableForm, setDisableForm] = React.useState<boolean>(true);
    const [tempMarks, setTempMarks] = React.useState<Mark[]>([]);
    const [fanSpeedMarks, setFanSpeedMarks] = React.useState<Mark[]>([]);
    const [deviceCapabilities, setDeviceCapabilities] = React.useState<DeviceCapabilities | null>(null);
    const [seeScheduled, setSeeScheduled] = React.useState<boolean>(false);
    const [schedules, setSchedules] = React.useState<Scheduled[]>([]);
    const [checked, setChecked] = React.useState<number[]>([]);
    const [isCommandSuccess, setIsCommandSuccess] = React.useState<number>(0);
    const [isScheduleSuccess, setIsScheduleSuccess] = React.useState<number>(0);
    const [isLoading, setIsLoading] = React.useState<boolean>(true);
    const firstStatusLoad = useRef(true);
    const [onOffOrdinal, setOnOffOrdinal] = React.useState<number>(1);
    const [changeOrdinal, setChangeOrdinal] = React.useState<number>(1);
    const [schedulesOrdinal, setSchedulesOrdinal] = React.useState<number>(1);
    const defaultParams : CommandParams = {
        userId: -1,
        unit: 'C',
        target: 23,
        fanSpeed: 2,
        currentTemp: -1,
        health: false,
        fungus: false,
        mode: ACMode.HEAT,
        everyDay: false,
        from: 0,
        to: 0,
        taskId: 0
    }
    const defaultConfig : Command = {
        deviceId: deviceId,
        commandType: CommandType.OFF,
        commandParams: defaultParams
    }
    const [currentConfig,setCurrentConfig] = React.useState<Command>(defaultConfig);
    const emptyState : ACValueDigest = {
        deviceId: deviceId,
        currentTemp: -1,
        targetTemp: 23,
        unit: 'C',
        mode: "AUTO",
        fanSpeed: 1,
        health: false,
        fungusPrevent: false
    }

    const deleteChecked = () => {
        checked.forEach((id) => {
            const command : Command = {
                deviceId: deviceId,
                commandType: CommandType.CANCEL_SCHEDULED,
                commandParams: {
                    userId: +sessionStorage.getItem("id")!,
                    unit: '',
                    target: -1,
                    fanSpeed: -1,
                    currentTemp: -1,
                    health: false,
                    fungus: false,
                    mode: ACMode.HEAT,
                    everyDay: false,
                    from: 0,
                    to: 0,
                    taskId: +id
                }
            }
            client.current!.send("/app/command/ac", {}, JSON.stringify(command));
            console.log("Sent CANCEL (" + schedulesOrdinal + ") - " + new Date());
        });
    }

    const close = () => {
        setCurrentState(emptyState);
        setCurrentStatus("Offline");
        setCurrentStatusColor("grey");
        setFungusChecked(false);
        setHealthChecked(false);
        setTargetTemp(+deviceCapabilities?.capabilities.get("minTemperature")!);
        setFanSpeed(1);
        setMode("");
        handleClose();
    }

    const back = () => {
        setSeeScheduled(false);
    }
    const [currentState, setCurrentState] = React.useState<ACValueDigest>(emptyState);
    const handleMode = (
        _event: React.MouseEvent<HTMLElement>,
        newMode: string | null,
    ) => {
        setMode(newMode);
        if(newMode == "AUTO")
            setFanSpeedDisable(true);
        else
            setFanSpeedDisable(false);
    };

    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);
    };

    const handleSwitchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setIsOn(event.target.checked);
    };

    const handleTempChange = (_event: Event, newValue: number | number[]) => {
        setTargetTemp(newValue as number);
    };

    const handleFanSpeedChange = (_event: Event, newValue: number | number[]) => {
        setFanSpeed(newValue as number);
    };

    const handleHealthChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setHealthChecked(event.target.checked);
    };

    const handleFungusChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setFungusChecked(event.target.checked);
    };

    const handleScheduleCheckedChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setScheduledChecked(event.target.checked);
    };

    const handleRepeatCheckedChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setRepeatChecked(event.target.checked);
    };

    const connectSocket = () => {
        try {
            client.current = Stomp.over(new SockJS(realtimeUrl));
            client.current.connect(
                {},
                () => {
                    console.log(':::::: SOCKET CONNECTED ::::::');
                    client.current!.subscribe('/ac/freshest/' + deviceId, onMessageReceived);
                    client.current!.subscribe("/ac/status/" + deviceId, onStatusReceived);
                    client.current!.send("/app/capabilities/ac", {}, deviceId.toString());
                    console.log("Sent capabilities request: " + new Date());
                    client.current!.subscribe("/ac/schedules/" + deviceId,onSchedulesReceived);
                    client.current!.subscribe("/ac/capabilities/" + deviceId,onCapabilitiesReceived);
                    getSchedules();
                    },
                () => {
                    console.log(':::::: SOCKET TRYING TO RECONNECT ::::::');
                }
            );
        } catch (err) {
            console.log(
                ':::::: ERROR: SOCKET CONNECTION ::::::' + JSON.stringify(err)
            );
        }
    };

    const onSchedulesReceived = (payload : Message) => {
        console.log("Received SCHEDULES (" + schedulesOrdinal + ") - " + new Date());
        setSchedulesOrdinal(schedulesOrdinal + 1);
        const schedules : Scheduled[] = JSON.parse(payload.body);
        setSchedules(schedules);
    }

    const getSchedules = () => {
        const params : CommandParams = {
            userId: +sessionStorage.getItem("id")!,
            unit: '',
            target: -1,
            fanSpeed: -1,
            health: false,
            currentTemp: -1,
            fungus: false,
            mode: ACMode.AUTO,
            everyDay: false,
            from: 0,
            to: 0,
            taskId: 0
        }

        const command : Command = {
            deviceId: deviceId,
            commandType: CommandType.GET_SCHEDULES,
            commandParams: params
        }
        client.current!.send("/app/command/ac", {}, JSON.stringify(command));
        console.log("Sent GET SCHEDULES (" + schedulesOrdinal + ") - " + new Date());
        setSchedulesOrdinal(schedulesOrdinal + 1);
    }
    const onStatusReceived = (payload : Message) => {
        console.log("Received STATUS (" + onOffOrdinal + ") - " + new Date());
        setOnOffOrdinal(onOffOrdinal + 1);
        lastStatusReceived.current = new Date().getTime();
        setInterval(() => {
           if(Math.abs(lastStatusReceived.current - new Date().getTime()) >= 30 * 1000) {
               setCurrentStatus("Offline");
               setCurrentStatusColor('grey');
               setDisableForm(true);
           }
        }, 30 * 1000)
        if(payload.body == "ON") {
            currentConfig.commandType = CommandType.ON;
            setCurrentConfig(currentConfig);
            if(firstStatusLoad.current) {
                setIsOn(true);
                firstStatusLoad.current = false;
            }
            setCurrentStatus("ON")
            setCurrentStatusColor("green");
            setDisableForm(false);
        }
        else {
            currentConfig.commandType = CommandType.OFF;
            setCurrentConfig(currentConfig);
            if(firstStatusLoad.current) {
                setIsOn(false);
                firstStatusLoad.current = false;
            }
            setCurrentStatus("OFF")
            setCurrentStatusColor("red");
            setDisableForm(false);
        }
        setIsLoading(false);
    }

    const onCapabilitiesReceived = (payload : Message) => {
       console.log("Received capabilities: " + new Date())
       const capabilities  = JSON.parse(payload.body);
       const map = new Map<string,string>();
       for(let val in capabilities.capabilities) {
           map.set(val, capabilities.capabilities[val]);
       }
       capabilities.capabilities = map;
       const fanSpeedMarksNew : Mark[] = [];
       const tempMarksNew : Mark[] = [];
       for (let i = 1; i <= +map.get("fanSpeed")!; i++) {
           const mark : Mark = {
               value: i,
               label: i.toString()
           }
           fanSpeedMarksNew.push(mark);
       }
       tempMarksNew.push({value: +map.get("minTemperature")!, label: map.get("minTemperature")!},
           {value: Math.floor((+map.get("maxTemperature")! + +map.get("minTemperature")!) / 2), label:  Math.floor((+map.get("maxTemperature")! + +map.get("minTemperature")!) / 2).toString()},
           {value: +map.get("maxTemperature")!, label: map.get("maxTemperature")!}
           )
        setTempMarks(tempMarksNew);
        setFanSpeedMarks(fanSpeedMarksNew);
       currentState.unit = capabilities.capabilities.get("temperatureUnit");
       setCurrentState(currentState);
       setDeviceCapabilities(capabilities);
    }

    const onMessageReceived = (payload : Message) => {
        const val : ACValueDigest =  JSON.parse(payload.body);
        setCurrentState(val);
        if(firstLoad.current) {
            setFanSpeed(val.fanSpeed);
            setMode(val.mode);
            setHealthChecked(val.health);
            setFungusChecked(val.fungusPrevent);
            setTargetTemp(val.targetTemp);
            firstLoad.current = false;
        }
    }


    useEffect(() => {
        if(!openSocket) {
            if(client.current != null) {
                client.current.disconnect(() => {})
            }
            return;
        } else {
            connectSocket();
        }
    }, [openSocket]);

    const sendCommand = () => {
        let commandType: CommandType = CommandType.ON;
        let currentTemp = -1;
        let fanSpeedInt = 3;
        if(isOn && currentConfig.commandType == CommandType.OFF)
            commandType = CommandType.ON
        if(!isOn)
            commandType = CommandType.OFF
        if(isOn && (currentConfig.commandType == CommandType.ON || currentConfig.commandType == CommandType.CHANGE)) {
            commandType = CommandType.CHANGE
            currentTemp = currentState.currentTemp
        }
        if(mode != ACMode.AUTO){
            fanSpeedInt = fanSpeed
        }
        const params : CommandParams = {
            userId: +sessionStorage.getItem("id")!,
            unit: deviceCapabilities?.capabilities.get("temperatureUnit")!,
            target: targetTemp,
            fanSpeed: fanSpeedInt,
            health: healthChecked,
            currentTemp: currentTemp,
            fungus: fungusChecked,
            mode: mode as ACMode,
            everyDay: false,
            from: 0,
            to: 0,
            taskId: 0
        }


        const command : Command = {
            deviceId: deviceId,
            commandType: commandType,
            commandParams: params
        }
        client.current!.send("/app/command/ac", {}, JSON.stringify(command));
        console.log("Sent CHANGE (" + changeOrdinal + ") - " + new Date());
        setChangeOrdinal(changeOrdinal + 1);
        currentConfig.commandType = commandType;
        setIsCommandSuccess(1);
    }

    const schedule = () => {
        if(from == null || to == null) {
            setErrorMessage("Specify the scheduled time span!");
            setIsSuccess(false);
            setErrorPopupOpen(true);
            setIsScheduleSuccess(2);
            return;
        }
        const fromLocal : number = from!.valueOf();
        let toLocal : number = to!.valueOf();
        let fanSpeedInt = 1;
        if(from > to) {
            toLocal = toLocal + 1000 * 60 * 60 * 24;
        }
        let valid = true;
        schedules.forEach((task) => {
            if(!((fromLocal >= task.to && toLocal > task.to) || (fromLocal < task.from && toLocal <= task.from)))
            {
                setErrorMessage("You already have scheduled cycle at this time span!");
                setIsSuccess(false);
                setErrorPopupOpen(true);
                setIsScheduleSuccess(2);
                valid = false;
            }
        })
        if(!valid) return;
        if(mode != ACMode.AUTO){
            fanSpeedInt = fanSpeed
        }
        const params : CommandParams = {
            userId: +sessionStorage.getItem("id")!,
            unit: deviceCapabilities?.capabilities.get("temperatureUnit")!,
            target: targetTemp,
            fanSpeed: fanSpeedInt,
            health: healthChecked,
            currentTemp: -1,
            fungus: fungusChecked,
            mode: mode as ACMode,
            everyDay: repeatChecked,
            from: fromLocal,
            to: toLocal,
            taskId: 0
        }
        const command : Command = {
            deviceId: deviceId,
            commandType: CommandType.SCHEDULE,
            commandParams: params
        }
        client.current!.send("/app/command/ac", {}, JSON.stringify(command));
        console.log("Sent SCHEDULE (" + schedulesOrdinal + ") - " + new Date());
        setSchedulesOrdinal(schedulesOrdinal + 1);
        setIsScheduleSuccess(1);
    }

    return (
        <React.Fragment>
        <Dialog
            maxWidth={'sm'}
            fullWidth={true}
            open={open}
            onClose={handleClose}>
            {!isLoading? null :
                <CircularProgress sx={{position:'absolute',right:'50px',top:'8px'}} />
            }
            <DialogTitle textAlign={'center'}>Remote</DialogTitle>
            <DialogContent>
                {!seeScheduled ?

                <Grid container rowSpacing={3}>

                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12} mt={2}>
                            <Grid item container xs={12} sm={12} md={6} lg={6} xl={6}>
                                <Typography variant={"body1"} display={'inline'}>Current Status:&nbsp;</Typography>
                                <Typography color={currentStatusColor} display={'inline'}><b> {currentStatus}</b></Typography>
                            </Grid>
                            <Grid item container xs={12} sm={12} md={6} lg={6} xl={6} justifyContent={'right'}>
                                <Typography variant={"body1"}>Current Temperature: <b>{currentState.currentTemp == -1? "-" : Math.floor(currentState.currentTemp)}°{currentState.unit}</b></Typography>
                            </Grid>
                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12} alignItems={'center'}>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={4}>
                                <Typography variant={"body1"} display={'inline'}>Set Temperature:</Typography>
                            </Grid>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={8}>
                                <Slider
                                    sx={{margin:'0'}}
                                    value={targetTemp}
                                    disabled={disableForm}
                                    onChange={handleTempChange}
                                    aria-label="Temperature"
                                    valueLabelDisplay="on"
                                    step={1}
                                    marks={tempMarks}
                                    min={deviceCapabilities == null ? 14 : +deviceCapabilities?.capabilities.get("minTemperature")!}
                                    max={deviceCapabilities == null ? 38 : +deviceCapabilities?.capabilities.get("maxTemperature")!}/>
                            </Grid>
                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}
                              mt={3}
                              columnSpacing={1} alignItems={'center'}>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={4}>
                                <Typography variant={"body1"} display={'inline'}>Fan Speed:</Typography>
                            </Grid>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={8}>
                                <Slider
                                    aria-label="Fan Speed"
                                    value={fanSpeed}
                                    disabled={fanSpeedDisable || disableForm}
                                    onChange={handleFanSpeedChange}
                                    valueLabelDisplay="on"
                                    sx={{margin:'0'}}
                                    step={1}
                                    marks={fanSpeedMarks}
                                    min={1}
                                    max={deviceCapabilities == null ? 1 : +deviceCapabilities?.capabilities.get("fanSpeed")!}/>
                            </Grid>
                        </Grid>

                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12} alignItems={'center'}>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={3}>
                                <Typography variant={"body1"} display={'inline'}>Set Mode:</Typography>
                            </Grid>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={9}>
                                <ToggleButtonGroup
                                    color="primary"
                                    value={mode}
                                    disabled={disableForm}
                                    exclusive
                                    onChange={handleMode}
                                    aria-label="mode">
                                    <ToggleButton value="HEAT" aria-label="heat"
                                                  disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("heating"!) != "true"}
                                                  selected={mode == "HEAT"}>
                                        <LightModeIcon/> Heat
                                    </ToggleButton>
                                    <ToggleButton value="COOL" aria-label="cool"
                                                  disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("cooling"!) != "true"}
                                                  selected={mode == "COOL"}>
                                        <AcUnitIcon/> Cool
                                    </ToggleButton>
                                    <ToggleButton value="DRY" aria-label="dry"
                                                  disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("dry"!) != "true"}
                                                  selected={mode == "DRY"}>
                                        <AirIcon/> Vent
                                    </ToggleButton>
                                    <ToggleButton value="AUTO" aria-label="auto"
                                                  disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("auto"!) != "true"}
                                                  selected={mode == "AUTO"}>
                                        <HdrAutoIcon/> Auto
                                    </ToggleButton>
                                </ToggleButtonGroup>
                            </Grid>
                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}
                              columnSpacing={1} alignItems={'center'} justifyContent={'center'} columnGap={3}>
                            <FormControlLabel control={<Checkbox
                                checked={healthChecked}
                                disabled={disableForm || deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("health"!) != "true"}
                                onChange={handleHealthChange}
                            />} label="Health/Ionizing Air" />
                            <FormControlLabel control={<Checkbox
                                disabled={disableForm || deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("fungusPrevention"!) != "true"}
                                checked={fungusChecked}
                                onChange={handleFungusChange}
                            />} label="Fungus Prevention" />
                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}
                              alignItems={'center'}
                              justifyContent={'center'}>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={3}>
                                <Stack direction="row" spacing={1} alignItems="center">
                                    <Typography>Off</Typography>
                                    <AntSwitch checked={isOn} disabled={disableForm} onChange={handleSwitchChange}/>
                                    <Typography>On</Typography>
                                </Stack>
                            </Grid>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={3} ml={5} direction={'column'} alignItems={'center'}>
                                <Button color={'secondary'}
                                        disabled={disableForm}
                                        variant={'contained'}
                                        onClick={sendCommand}>Send</Button>
                            </Grid>

                            <CheckIcon color={'success'} visibility={isCommandSuccess != 1 ? 'hidden' : 'visible'}/>
                            <CloseIcon color={'error'} visibility={isCommandSuccess != 2 ? 'hidden' : 'visible'}/>
                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12} alignItems={'center'}
                        columnSpacing={2}>
                            <Grid item xs={12} sm={12} md={12} lg={12} xl={3}>
                                <FormControlLabel control={<Checkbox
                                    checked={scheduledChecked}
                                    disabled={disableForm}
                                    onChange={handleScheduleCheckedChange}
                                />} label="Scheduled" />
                            </Grid>
                            <Grid item xs={12} sm={12} md={12} lg={5} xl={4} mr={3}>
                                <LocalizationProvider dateAdapter={AdapterDayjs}>
                                    <MobileTimePicker  label={'From'}
                                                       value={from}
                                                       onChange={(newValue) => setFrom(newValue)}
                                                       disabled={!scheduledChecked || disableForm}/>
                                </LocalizationProvider>
                            </Grid>
                            <Grid item xs={12} sm={12} md={12} lg={5} xl={4}>
                                <LocalizationProvider dateAdapter={AdapterDayjs}>
                                    <MobileTimePicker   label={'To'}
                                                        value={to}
                                                        onChange={(newValue) => setTo(newValue)}
                                                        disabled={!scheduledChecked || disableForm}/>
                                </LocalizationProvider>
                            </Grid>
                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12} justifyContent={'center'}>
                            <FormControlLabel control={<Checkbox
                                disabled={!scheduledChecked || disableForm}
                                checked={repeatChecked}
                                onChange={handleRepeatCheckedChange}
                            />} label="Repeat Daily" />
                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12} mb={1}  justifyContent={'center'} alignItems={'center'}>
                            <Button variant={'contained'}
                                    onClick={schedule}
                                    sx={{marginRight:5}}
                                    disabled={!scheduledChecked || disableForm}
                                    color={'primary'}>Schedule Cycle</Button>
                            <CheckIcon color={'success'} visibility={isScheduleSuccess != 1 ?'hidden' : 'visible'}/>
                            <CloseIcon color={'error'} visibility={isScheduleSuccess != 2 ? 'hidden' : 'visible'}/>
                        </Grid>
                       

                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12} mb={1}  justifyContent={'center'}>
                            <Button variant="text"
                                    sx={{color:'blue'}}
                                    onClick={() => {setSeeScheduled(true);}}>See Schedules</Button>
                        </Grid>

                </Grid>
                    :
                <Grid rowSpacing={3} height={600} justifyContent={'center'}>
                    <IconButton aria-label="back" sx={{position:'absolute',top:'2px',left:'3px'}} onClick={back}>
                        <ChevronLeftIcon />
                    </IconButton>
                    <DataGrid
                        sx={{height:500}}
                        rows={schedules}
                        disableColumnMenu={true}
                        getRowId={(val) => val.id}
                        columns={columnsScheduled}
                        hideFooterPagination={true}
                        onRowSelectionModelChange={(model) => {
                            console.log(model);
                            setChecked(model as [])}}
                        checkboxSelection
                    />
                    <Grid item container xs={12} sm={12} md={12} lg={12} xl={12} justifyContent={'center'}>
                    <Button variant={'contained'}
                            onClick={deleteChecked}
                            sx={{marginTop:'1em'}}
                            color={'primary'}>Delete Selected</Button>
                    </Grid>
                </Grid>
            }
            <IconButton aria-label="close" sx={{position:'absolute',top:'2px',right:'3px'}} onClick={close}>
                <CloseIcon />
            </IconButton>
            </DialogContent>

        </Dialog>
        <PopupMessage message={errorMessage} isSuccess={isSuccess} handleClose={handleErrorPopupClose}
                      open={errorPopupOpen}/>
        </React.Fragment>

    );
}