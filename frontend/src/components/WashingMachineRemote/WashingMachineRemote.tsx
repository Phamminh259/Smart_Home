// @ts-nocheck
import {
    Button,
    Checkbox,
    CircularProgress,
    Dialog,
    DialogContent,
    DialogTitle,
    FormControlLabel,
    Grid,
    IconButton,
    Slider,
    ToggleButton,
    ToggleButtonGroup,
    Typography
} from "@mui/material";
import React, {useEffect, useRef} from "react";
import {LocalizationProvider, MobileTimePicker} from "@mui/x-date-pickers";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import CloseIcon from "@mui/icons-material/Close";
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import Stomp, {Client, Message} from "stompjs";
import SockJS from "sockjs-client";
import dayjs, {Dayjs} from "dayjs";
import {CommandType} from "../../models/enums/CommandType.ts";
import {PopupMessage} from "../PopupMessage/PopupMessage.tsx";
import {DeviceCapabilities} from "../../models/DeviceCapabilities.ts";
import {Command} from "../../models/Command.ts";
import {DataGrid, GridColDef, GridValueGetterParams} from "@mui/x-data-grid";
import {Scheduled} from "../../models/Scheduled.ts";
import CheckIcon from '@mui/icons-material/Check';
import {WashingMachineCommandParams} from "../../models/WashingMachineCommandParams.ts";
import {WashingMachineMode} from "../../models/enums/WashingMachineMode.ts";
import {WashingMachineValueDigest} from "../../models/WashingMachineValueDigest.ts";

interface WashingMachineRemoteProps {
    open: boolean,
    handleClose: () => void,
    deviceId: number,
    openSocket: boolean,
}

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
export function WashingMachineRemote ({open,handleClose, deviceId, openSocket} : WashingMachineRemoteProps)  {

    const [errorMessage, setErrorMessage] = React.useState<string>("");
    const [errorPopupOpen, setErrorPopupOpen] = React.useState<boolean>(false);
    const [isSuccess, setIsSuccess] = React.useState(true);
    const [scheduledChecked, setScheduledChecked] = React.useState(false);
    const [mode, setMode] = React.useState<string | null>("COTTONS");
    const [centrifugeSpeed, setCentrifugeSpeed] = React.useState<number>(800);
    const [temp, setTemp] = React.useState<number>(60);
    const realtimeUrl = "http://localhost:8080/realtime";
    const client = useRef<Client | null>(null);
    const [currentStatus, setCurrentStatus] = React.useState<string>("Offline");
    const [currentStatusColor, setCurrentStatusColor] = React.useState<string>('grey');
    const firstLoad = useRef(true);
    const lastStatusReceived = useRef(0);
    const [modeName, setModeName] = React.useState<string>("-");
    const [from, setFrom] = React.useState<Dayjs | null>(null);
    const [disableForm, setDisableForm] = React.useState<boolean>(true);
    const [tempMarks, setTempMarks] = React.useState<Mark[]>([]);
    const [centrifugeSpeedMarks, setCentrifugeSpeedMarks] = React.useState<Mark[]>([]);
    const [deviceCapabilities, setDeviceCapabilities] = React.useState<DeviceCapabilities | null>(null);
    const [seeScheduled, setSeeScheduled] = React.useState<boolean>(false);
    const [schedules, setSchedules] = React.useState<Scheduled[]>([]);
    const [checked, setChecked] = React.useState<number[]>([]);
    const [isCommandSuccess, setIsCommandSuccess] = React.useState<number>(0);
    const [isScheduleSuccess, setIsScheduleSuccess] = React.useState<number>(0);
    const [isLoading, setIsLoading] = React.useState<boolean>(true);
    const firstStatusLoad = useRef(true);
    const [canChange, setCanChange] = React.useState<boolean>(true);
    const [disableTemp, setDisableTemp] = React.useState<boolean>(false);
    const [disableCent, setDisableCent] = React.useState<boolean>(false);
    const onOffOrdinal = React.useRef(1);
    const schedulesOrdinal= React.useRef(1);
    const defaultParams : WashingMachineCommandParams = {
        userId: -1,
        unit: 'C',
        centrifugeSpeed: 800,
        temp: 60,
        mode: WashingMachineMode.COTTONS,
        from: 0,
        taskId: 0
    }
    const defaultConfig : Command = {
        deviceId: deviceId,
        commandType: CommandType.OFF,
        commandParams: defaultParams
    }
    const [currentConfig,setCurrentConfig] = React.useState<Command>(defaultConfig);
    const emptyState : WashingMachineValueDigest = {
        deviceId: deviceId,
        centrifugeSpeed: 800,
        temperature: 60,
        unit: 'C',
        mode: ""
    }

    const deleteChecked = () => {
        checked.forEach((id) => {
            const command : Command = {
                deviceId: deviceId,
                commandType: CommandType.CANCEL_SCHEDULED,
                commandParams: {
                    userId: +sessionStorage.getItem("id")!,
                    unit: '',
                    centrifugeSpeed: -1,
                    temp: -1,
                    mode: WashingMachineMode.COTTONS,
                    from: 0,
                    taskId: +id
                }
            }
            client.current!.send("/app/command/wm", {}, JSON.stringify(command));
            console.log("Sent CANCEL (" + schedulesOrdinal.current + ") - " + new Date());
            schedulesOrdinal.current += 1;
        });
    }

    const close = () => {
        setCurrentState(emptyState);
        setCurrentStatus("Offline");
        setCurrentStatusColor("grey");
        setTemp(+deviceCapabilities?.capabilities.get("minTemperature")!);
        setCentrifugeSpeed(+deviceCapabilities?.capabilities.get("minCentrifuge")!);
        setMode("");
        handleClose();
    }

    const back = () => {
        setSeeScheduled(false);
    }
    const [currentState, setCurrentState] = React.useState<WashingMachineValueDigest>(emptyState);
    const handleMode = (
        _event: React.MouseEvent<HTMLElement>,
        newMode: string | null,
    ) => {
        setMode(newMode);
        let tempLocal = 0;
        let centrifugeSpeedLocal = 0;
        setDisableTemp(false);
        setDisableCent(false);
        if(newMode == "COTTONS" || newMode == "DUVET") {
            tempLocal = 60;
            centrifugeSpeedLocal = 800;
        }
        else if (newMode == "SYNTHETICS" || newMode == "SHIRTS") {
            tempLocal = 60;
            centrifugeSpeedLocal = 600;
        }
        else if (newMode == "DAILY_EXPRESS") {
            tempLocal = 60;
            centrifugeSpeedLocal = 1000;
        }
        else if (newMode == "WOOL" || newMode == "DARK_WASH" || newMode == "MIXED") {
            tempLocal = 40;
            centrifugeSpeedLocal = 800;
        }
        else if (newMode == "OUTDOOR") {
            tempLocal = 40;
            centrifugeSpeedLocal = 1000;
        }
        else if (newMode == "RINSE_SPIN" || newMode == "SPIN_ONLY") {
            centrifugeSpeedLocal = 1200;
            setDisableTemp(true);
        }
        else if(newMode == "STEAM") {
            setDisableCent(true);
            setDisableTemp(true);
        }
        else if (newMode == "HYGIENE") {
            tempLocal = 90;
            centrifugeSpeedLocal = 1200;
        }
        if(tempLocal > +deviceCapabilities?.capabilities.get("maxTemperature")!)
            tempLocal = +deviceCapabilities?.capabilities.get("maxTemperature")!

        if(tempLocal < +deviceCapabilities?.capabilities.get("minTemperature")!)
            tempLocal = +deviceCapabilities?.capabilities.get("minTemperature")!

        if(centrifugeSpeedLocal > +deviceCapabilities?.capabilities.get("maxCentrifuge")!)
            centrifugeSpeedLocal = +deviceCapabilities?.capabilities.get("maxCentrifuge")!

        if(centrifugeSpeedLocal < +deviceCapabilities?.capabilities.get("minCentrifuge")!)
           centrifugeSpeedLocal =  +deviceCapabilities?.capabilities.get("minCentrifuge")!

        setTemp(tempLocal);
        setCentrifugeSpeed(centrifugeSpeedLocal);
    };

    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);
    };

    const handleTempChange = (_event: Event, newValue: number | number[]) => {
        setTemp(newValue as number);
    };

    const handleCentrifugeSpeedChange = (_event: Event, newValue: number | number[]) => {
        setCentrifugeSpeed(newValue as number);
    };


    const handleScheduleCheckedChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setScheduledChecked(event.target.checked);
    };

    const connectSocket = () => {
        try {
            client.current = Stomp.over(new SockJS(realtimeUrl));
            client.current.connect(
                {},
                () => {
                    console.log(':::::: SOCKET CONNECTED ::::::');
                    client.current!.subscribe('/wm/freshest/' + deviceId, onMessageReceived);
                    client.current!.subscribe("/wm/status/" + deviceId, onStatusReceived);
                    client.current!.send("/app/capabilities/wm", {}, deviceId.toString());
                    console.log("Sent CAPABILITIES - " + new Date());

                    client.current!.subscribe("/ac/schedules/" + deviceId,onSchedulesReceived);
                    client.current!.subscribe("/wm/capabilities/" + deviceId,onCapabilitiesReceived);
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
        console.log("Received SCHEDULES (" + schedulesOrdinal.current + ") - " + new Date());
        schedulesOrdinal.current += 1;
        const schedules : Scheduled[] = JSON.parse(payload.body);
        setSchedules(schedules);
    }

    const getSchedules = () => {
        const params : WashingMachineCommandParams = {
            userId: +sessionStorage.getItem("id")!,
            unit: '',
            centrifugeSpeed: -1,
            temp: -1,
            mode: WashingMachineMode.COTTONS,
            from: 0,
            taskId: 0
        }

        const command : Command = {
            deviceId: deviceId,
            commandType: CommandType.GET_SCHEDULES,
            commandParams: params
        }
        client.current!.send("/app/command/wm", {}, JSON.stringify(command));
        console.log("Sent SCHEDULES GET (" + schedulesOrdinal.current + ") - " + new Date());
        schedulesOrdinal.current += 1;
    }
    const onStatusReceived = (payload : Message) => {
        console.log("Received STATUS (" + onOffOrdinal.current + ") - " + new Date());
        onOffOrdinal.current += 1;
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
                firstStatusLoad.current = false;
            }
            setCurrentStatus("ON")
            setCurrentStatusColor("green");
            setDisableForm(false);
            setCanChange(false);
        }
        else {
            currentConfig.commandType = CommandType.OFF;
            setCurrentConfig(currentConfig);
            if(firstStatusLoad.current) {
                firstStatusLoad.current = false;
            }
            setCurrentStatus("OFF")
            setCurrentStatusColor("red");
            currentState.mode = "-";
            setModeName("-");
            setCurrentState(currentState);
            setDisableForm(false);
            setCanChange(true);
        }
        setIsLoading(false);
    }

    const onCapabilitiesReceived = (payload : Message) => {
        console.log("Received CAPABILITIES - " + new Date());
        const capabilities  = JSON.parse(payload.body);
        const map = new Map<string,string>();
        for(let val in capabilities.capabilities) {
            map.set(val, capabilities.capabilities[val]);
        }
        capabilities.capabilities = map;
        const centrifugeSpeedMarksNew : Mark[] = [];
        const tempMarksNew : Mark[] = [];
        for (let i = +map.get("minCentrifuge")!; i <= +map.get("maxCentrifuge")!; i+= 200) {
            const mark : Mark = {
                value: i,
                label: i.toString()
            }
            centrifugeSpeedMarksNew.push(mark);
        }
        tempMarksNew.push({value: +map.get("minTemperature")!, label: map.get("minTemperature")!},
            {value: Math.floor((+map.get("maxTemperature")! + +map.get("minTemperature")!) / 2), label:  Math.floor((+map.get("maxTemperature")! + +map.get("minTemperature")!) / 2).toString()},
            {value: +map.get("maxTemperature")!, label: map.get("maxTemperature")!}
        )
        setTempMarks(tempMarksNew);
        setCentrifugeSpeedMarks(centrifugeSpeedMarksNew);
        currentState.unit = capabilities.capabilities.get("temperatureUnit");
        setTemp(capabilities.capabilities.get("minTemperature"));
        setCentrifugeSpeed(capabilities.capabilities.get("minCentrifuge"));
        setCurrentState(currentState);
        setDeviceCapabilities(capabilities);
    }

    const onMessageReceived = (payload : Message) => {
        const val : WashingMachineValueDigest =  JSON.parse(payload.body);
        setCurrentState(val);
        setModeName(val.mode)
        if(val.mode == "RINSE_SPIN")
            setModeName("RINSE & SPIN");
        if(val.mode == "SPIN_ONLY")
            setModeName("SPIN ONLY");
        if(val.mode == "DARK_WASH")
            setModeName("DARK WASH");
        if(val.mode == "DAILY_EXPRESS")
            setModeName("DAILY EXPRESS");
        if(firstLoad.current) {
            setCentrifugeSpeed(val.centrifugeSpeed);
            setMode(val.mode);
            setTemp(val.temperature);
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
        const params : WashingMachineCommandParams = {
            userId: +sessionStorage.getItem("id")!,
            unit: deviceCapabilities?.capabilities.get("temperatureUnit")!,
            centrifugeSpeed: centrifugeSpeed,
            temp: temp,
            mode: mode as WashingMachineMode,
            from: 0,
            taskId: 0
        }

        const command : Command = {
            deviceId: deviceId,
            commandType: commandType,
            commandParams: params
        }
        client.current!.send("/app/command/wm", {}, JSON.stringify(command));
        console.log("Send ON (" + onOffOrdinal.current + ") - " + new Date());
        onOffOrdinal.current  += 1;
        currentConfig.commandType = commandType;
        setIsCommandSuccess(1);
    }

    const schedule = () => {
        if(from == null) {
            setErrorMessage("Specify the scheduled time span!");
            setIsSuccess(false);
            setErrorPopupOpen(true);
            setIsScheduleSuccess(2);
            return;
        }
        let fromLocal : number = from!.valueOf();
        let now = dayjs();
        if(from < now) {
            fromLocal = fromLocal + 1000 * 60 * 60 * 24;
        }
        let valid = true;
        schedules.forEach((task) => {
            if(fromLocal >= task.from && fromLocal < task.to)
            {
                setErrorMessage("You already have scheduled cycle at this time span!");
                setIsSuccess(false);
                setErrorPopupOpen(true);
                setIsScheduleSuccess(2);
                valid = false;
            }
        })
        if(!valid) return;

        const params : WashingMachineCommandParams = {
            userId: +sessionStorage.getItem("id")!,
            unit: deviceCapabilities?.capabilities.get("temperatureUnit")!,
            centrifugeSpeed: centrifugeSpeed,
            temp: temp,
            mode: mode as WashingMachineMode,
            from: fromLocal,
            taskId: 0
        }
        const command : Command = {
            deviceId: deviceId,
            commandType: CommandType.SCHEDULE,
            commandParams: params
        }
        client.current!.send("/app/command/wm", {}, JSON.stringify(command));
        console.log("Sent SCHEDULE (" + schedulesOrdinal.current + ") - " + new Date());
        schedulesOrdinal.current += 1;
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
                                    <Typography variant={"body1"}>Current Mode: <b>{currentState.mode == ""? "-" : modeName}</b></Typography>
                                </Grid>
                            </Grid>
                            <Grid container item xs={12} sm={12} md={12} lg={12} xl={12} alignItems={'center'}>
                                <Grid item xs={12} sm={12} md={12} lg={12} xl={4}>
                                    <Typography variant={"body1"} display={'inline'}>Set Temperature:</Typography>
                                </Grid>
                                <Grid item xs={12} sm={12} md={12} lg={12} xl={8}>
                                    <Slider
                                        sx={{margin:'0'}}
                                        value={temp}
                                        disabled={disableForm || disableTemp}
                                        onChange={handleTempChange}
                                        aria-label="Temperature"
                                        valueLabelDisplay="on"
                                        step={10}
                                        marks={tempMarks}
                                        min={deviceCapabilities == null ? 40 : +deviceCapabilities?.capabilities.get("minTemperature")!}
                                        max={deviceCapabilities == null ? 95 : +deviceCapabilities?.capabilities.get("maxTemperature")!}/>
                                </Grid>
                            </Grid>
                            <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}
                                  mt={3}
                                  columnSpacing={1} alignItems={'center'}>
                                <Grid item xs={12} sm={12} md={12} lg={12} xl={4}>
                                    <Typography variant={"body1"} display={'inline'}>Centrifuge Speed:</Typography>
                                </Grid>
                                <Grid item xs={12} sm={12} md={12} lg={12} xl={8}>
                                    <Slider
                                        aria-label="Centrifuge Speed"
                                        value={centrifugeSpeed}
                                        onChange={handleCentrifugeSpeedChange}
                                        valueLabelDisplay="on"
                                        sx={{margin:'0'}}
                                        step={100}
                                        disabled={disableForm || disableCent}
                                        marks={centrifugeSpeedMarks}
                                        min={deviceCapabilities == null ? 400 : +deviceCapabilities?.capabilities.get("minCentrifuge")!}
                                        max={deviceCapabilities == null ? 1600 : +deviceCapabilities?.capabilities.get("maxCentrifuge")!}/>
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
                                        sx={{ flexWrap: "wrap"}}
                                        disabled={disableForm}
                                        exclusive
                                        onChange={handleMode}
                                        aria-label="mode">
                                        <ToggleButton value="COTTONS" aria-label="cottons"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("cottons"!) != "true"}
                                                      selected={mode == "COTTONS"}>COTTONS</ToggleButton>
                                        <ToggleButton value="SYNTHETICS" aria-label="synthetics"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("synthetics"!) != "true"}
                                                      selected={mode == "SYNTHETICS"}>SYNTHETICS</ToggleButton>
                                        <ToggleButton value="DAILY_EXPRESS" aria-label="daily_express"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("daily_express"!) != "true"}
                                                      selected={mode == "DAILY_EXPRESS"}>DAILY EXPRESS</ToggleButton>
                                        <ToggleButton value="WOOL" aria-label="wool"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("wool"!) != "true"}
                                                      selected={mode == "WOOL"}>WOOL</ToggleButton>
                                        <ToggleButton value="DARK_WASH" aria-label="dark_wash"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("dark_wash"!) != "true"}
                                                      selected={mode == "DARK_WASH"}>DARK WASH</ToggleButton>
                                        <ToggleButton value="OUTDOOR" aria-label="outdoor"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("outdoor"!) != "true"}
                                                      selected={mode == "OUTDOOR"}>OUTDOOR</ToggleButton>
                                        <ToggleButton value="SHIRTS" aria-label="shirts"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("shirts"!) != "true"}
                                                      selected={mode == "SHIRTS"}>SHIRTS</ToggleButton>
                                        <ToggleButton value="DUVET" aria-label="duvet"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("duvet"!) != "true"}
                                                      selected={mode == "DUVET"}>DUVET</ToggleButton>
                                        <ToggleButton value="MIXED" aria-label="mixed"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("mixed"!) != "true"}
                                                      selected={mode == "MIXED"}>MIXED</ToggleButton>
                                        <ToggleButton value="STEAM" aria-label="steam"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("steam"!) != "true"}
                                                      selected={mode == "STEAM"}>STEAM</ToggleButton>
                                        <ToggleButton value="RINSE_SPIN" aria-label="rinse_spin"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("rinse_spin"!) != "true"}
                                                      selected={mode == "RINSE_SPIN"}>RINSE & SPIN</ToggleButton>
                                        <ToggleButton value="SPIN_ONLY" aria-label="spin_only"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("spin_only"!) != "true"}
                                                      selected={mode == "SPIN_ONLY"}>SPIN ONLY</ToggleButton>
                                        <ToggleButton value="HYGIENE" aria-label="hygiene"
                                                      sx={{margin:'4px'}}
                                                      disabled={deviceCapabilities == null ? true : deviceCapabilities?.capabilities.get("hygiene"!) != "true"}
                                                      selected={mode == "HYGIENE"}>HYGIENE</ToggleButton>
                                    </ToggleButtonGroup>
                                </Grid>
                            </Grid>
                            <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}
                                  alignItems={'center'}
                                  justifyContent={'center'}>
                                    <Grid item container xs={12} sm={12} md={12} lg={8} xl={8}  alignItems={'center'} justifyContent={'center'}>
                                        <Button color={'secondary'}

                                                disabled={disableForm || !canChange}
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
                                <Grid item xs={12} sm={12} md={12} lg={8} xl={8} mr={3}>
                                    <LocalizationProvider dateAdapter={AdapterDayjs}>
                                        <MobileTimePicker  label={'When'}
                                                           value={from}
                                                           onChange={(newValue) => setFrom(newValue)}
                                                           disabled={!scheduledChecked || disableForm}/>
                                    </LocalizationProvider>
                                </Grid>
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