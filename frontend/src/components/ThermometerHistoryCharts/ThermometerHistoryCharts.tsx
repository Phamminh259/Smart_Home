// @ts-nocheck
import {
    Button,
    CircularProgress,
    CssBaseline,
    FormControl,
    FormControlLabel,
    Grid,
    InputLabel, MenuItem,
    Radio,
    RadioGroup, Select, SelectChangeEvent,
    Typography
} from "@mui/material";
import {SideNav} from "../Sidenav/SideNav.tsx";
import {UserService} from "../../services/UserService.ts";
import {DeviceService} from "../../services/DeviceService.ts";
import {PopupMessage} from "../PopupMessage/PopupMessage.tsx";
import {ResizableBox} from "../Shared/ResizableBox.tsx";
import React, {useEffect} from "react";
import {MeasurementRequest} from "../../models/MeasurementRequest.ts";
import {LineChart} from "@mui/x-charts";
import {DataPoint, LTTB} from 'downsample';
import {useNavigate} from "react-router-dom";
import {LocalizationProvider, MobileDateTimePicker} from "@mui/x-date-pickers";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {ChartData} from "../../models/ChartData.ts";
import {Dayjs} from "dayjs";
import {RoleEnum} from "../../models/enums/RoleEnum";

interface ThermometerChartsProps {
    userService: UserService
    deviceService: DeviceService
}

type ChartDataShort = {
    timestamp: Date,
    value: number,
}

export function ThermometerChartsHistory({userService, deviceService} : ThermometerChartsProps) {

    function dateFormatter (date: Date): string {
        return date.toLocaleDateString();
    }
    function timeFormatter (date: Date): string {
         return date.toLocaleTimeString();
    }
    const [errorMessage, setErrorMessage] = React.useState<string>("");
    const [errorPopupOpen, setErrorPopupOpen] = React.useState<boolean>(false);
    const [isSuccess, setIsSuccess] = React.useState(true);
    const [tempData, setTempData] = React.useState<ChartDataShort[]>([]);
    const [humidityData, setHumidityData] = React.useState<ChartDataShort[]>([]);
    const deviceId = String(location.pathname.split('/').pop());
    const navigate = useNavigate();
    const [units, setUnits] = React.useState<string>('C');
    const [timeSpan, setTimeSpan] = React.useState<string>("");
    const [from, setFrom] = React.useState<Dayjs | null>(null);
    const [to, setTo] = React.useState<Dayjs | null>(null);
    const [isCustom, setIsCustom] = React.useState<boolean>(false);
    const [isTimeFormatter, setIsTimeFormatter] = React.useState<boolean>(true);
    const [isLoading, setIsLoading] = React.useState<boolean>(false);
    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        changeUnit((event.target as HTMLInputElement).value);
        setUnits((event.target as HTMLInputElement).value);
    };

    const changeUnit = (unit : string) => {
        if(units == unit) return;
        const transformedData: ChartDataShort[] = [];
        if(unit == "F") {
            tempData.forEach((val) => {
                const transformed : ChartDataShort = {
                    timestamp: val.timestamp,
                    value: (val.value * 9/5) + 32
                }
                transformedData.push(transformed)
            })
        }
        else {
            tempData.forEach((val) => {
                const transformed : ChartDataShort = {
                    timestamp: val.timestamp,
                    value: (val.value - 32) * 5/9
                }
                transformedData.push(transformed)
            })
        }
        setTempData(transformedData);
    }
    const downsample = (data: ChartDataShort[], targetLength : number) => {
        const dataPoints : DataPoint[] = [];
        data.forEach((val) => {
            if(val.value != null) {
                const point: DataPoint = {
                    x: new Date(val.timestamp),
                    y: val.value
                }
                dataPoints.push(point);
            }
        })
        const res =  LTTB(dataPoints, targetLength);
        let result : ChartDataShort[] = [];
        for (let entry of res) {
            const data : ChartDataShort = {
                timestamp: entry.x,
                value: entry.y
            }
            result.push(data);
        }
        return result
    }

    const handleTimeSpanChange = (event: SelectChangeEvent) => {
        setTimeSpan(event.target.value);
        if(event.target.value == "C"){
            setIsCustom(true);
        }
        else {
            setIsCustom(false);
        }
    };

    async function getData  ()  {
        setHumidityData([]);
        setTempData([]);
        let fromLocal: Date | number = new Date();
        let toLocal = new Date().getTime();
        let targetLength = 0;
        if (timeSpan == "C" && to! <= from!) {
            setErrorMessage("Invalid date range!");
            setIsSuccess(false);
            setErrorPopupOpen(true);
            return;
        }
        if (timeSpan == "C" && to!.valueOf() - from!.valueOf() > 1000 * 60 * 60 * 24 * 30)
        {
            setErrorMessage("Date range must be less than a month!");
            setIsSuccess(false);
            setErrorPopupOpen(true);
            return;
        }
        setIsLoading(true);
        if (timeSpan == "1") {
            fromLocal = fromLocal.getTime() - 60 * 60 * 1000;
            targetLength = 60;
            setIsTimeFormatter(true);
        }
        else if (timeSpan == "3") {
            fromLocal = fromLocal.getTime() - 3 * 60 * 60 * 1000;
            targetLength = 100;
            setIsTimeFormatter(true);
        }
        else if (timeSpan == "6") {
            fromLocal = fromLocal.getTime() - 6 * 60 * 60 * 1000;
            targetLength = 130;
            setIsTimeFormatter(true);
        }
        else if (timeSpan == "12") {
            fromLocal = fromLocal.getTime() - 12 * 60 * 60 * 1000;
            targetLength = 150;
            setIsTimeFormatter(true);
        }
        else if (timeSpan == "24") {
            fromLocal = fromLocal.getTime() - 24 * 60 * 60 * 1000;
            targetLength = 150;
            setIsTimeFormatter(true);
        }
        else if (timeSpan == "W") {
            setIsTimeFormatter(false);
            fromLocal = fromLocal.getTime() - 7 * 24 * 60 * 60 * 1000;
            targetLength = 150;
        }
        else if (timeSpan == "M") {
            setIsTimeFormatter(false);
            fromLocal = fromLocal.getTime() - new Date(fromLocal.getFullYear(), fromLocal.getMonth() + 1, 0).getDate() * 24 * 60 * 60 * 1000;
            targetLength = 120;
        } else {
            setIsTimeFormatter(true);
            fromLocal = from!.valueOf();
            toLocal = to!.valueOf();
            if((toLocal - fromLocal) <= 1000 * 60 * 60)
                targetLength = 60
            else if ((toLocal - fromLocal) <= 1000 * 60 * 60 * 3)
                targetLength = 100;
            else if ((toLocal - fromLocal) <= 1000 * 60 * 60 * 6)
                targetLength = 130;
            else if ((toLocal - fromLocal) <= 1000 * 60 * 60 * 12)
                targetLength = 150;
            else if ((toLocal - fromLocal) <= 1000 * 60 * 60 * 24)
                targetLength = 150;
            else if ((toLocal - fromLocal) <= 1000 * 60 * 60 * 24 * 7) {
                targetLength = 150;
                setIsTimeFormatter(false);
            }
            else if ((toLocal - fromLocal) <= 1000 * 60 * 60 * 24 * 30) {
                setIsTimeFormatter(false);
                targetLength = 120;
            }

        }
        const tempDataRaw: ChartData[] = [];
        const humDataRaw: ChartData[] = [];
        await getMeasurement("temperature", fromLocal, toLocal).then((r) => {
            for(let i = 0; i < r.length; i++) {
                tempDataRaw.push(r[i])
            }
            });
        await getMeasurement("humidity", fromLocal, toLocal).then((r) => {
            for(let i = 0; i < r.length; i++) {
                humDataRaw.push(r[i])
            }
        });

        const preparedTemp = prepareData(tempDataRaw,targetLength);
        const preparedHum = prepareData(humDataRaw, targetLength);
        setTempData(preparedTemp);
        setHumidityData(preparedHum);
        setIsLoading(false);
        setUnits(tempDataRaw[0].tags["unit"]);
    }

    const prepareData = (data: ChartDataShort[], targetLength: number) => {
        const temp: ChartDataShort[] = [];
        data.forEach((value) => {
            const newVal: ChartDataShort = {
                timestamp: new Date(value.timestamp),
                value: value.value,
            };
            temp.push(newVal);
        });
        let downsampled : ChartDataShort[] = temp;
        if(temp.length > targetLength) {
            downsampled = downsample(data, targetLength);
        }
        return downsampled;
    }

    function getMeasurement (measurement: string, from: number, to: number) : Promise<ChartData[]>  {
        const request : MeasurementRequest = {
            from: Math.floor(from / 1000),
            to: Math.floor(to / 1000),
            deviceId: deviceId,
            measurementName: measurement
        }
        return deviceService.getDeviceMeasurements(request).then((response => {
           if (response.length != 0) {
               return response
           }
           return [];
       })).catch((err) => {
           console.log(err);
           setErrorMessage(err.response);
           setIsSuccess(false);
           setErrorPopupOpen(true);
           return [];
       });
    }

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

    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);

    };
    return (
        <>
            <CssBaseline/>
            <Grid container
                  height={'100%'}
                  direction={'row'}
                  justifyContent={"center"}>
                <Grid container className={'dark-background'} height={'100%'} justifyContent={'flex-start'}>
                    <Grid item xs={0} sm={0} md={2} lg={2} xl={2}>
                        <SideNav userService={userService} isAdmin={sessionStorage.getItem("role") == RoleEnum.ROLE_ADMIN ||
                            sessionStorage.getItem("role") == RoleEnum.ROLE_SUPERADMIN} isSuperadmin={sessionStorage.getItem("role") == RoleEnum.ROLE_SUPERADMIN}/>
                    </Grid>
                    <Grid
                        item
                        container
                        direction={'row'}
                        height={'100%'}
                        xl={10}
                        lg={10}
                        md={10}
                        sm={12}
                        xs={12}
                        p={2}
                        className={'white-background'}
                        style={{
                            borderRadius: '1.5em',
                            overflowY: 'auto',
                            maxHeight: '100vh',
                        }}
                        alignItems={'flex-start'}
                        ml={{xl: '20%', lg: '20%', md: '25%', sm: '0', xs: '0'}}
                        mt={{xl: 0, lg: 0, md: 0, sm: '64px', xs: '64px'}}>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}
                              alignItems={'center'}
                              mt={1}
                              ml={3}
                              pr={3}
                              mb={2}>
                            <Typography variant={'h6'} mr={2}>Filter By Time Span:</Typography>
                            <FormControl sx={{ m: 1, minWidth: 200 }}>
                                <InputLabel id="time span">Time Span</InputLabel>
                                <Select
                                    labelId="Time span picker"
                                    id="Time span"
                                    value={timeSpan}
                                    label="Time span"
                                    onChange={handleTimeSpanChange}>
                                    <MenuItem value={"1"}>Last 1h</MenuItem>
                                    <MenuItem value={"3"}>Last 3h</MenuItem>
                                    <MenuItem value={"6"}>Last 6h</MenuItem>
                                    <MenuItem value={"12"}>Last 12h</MenuItem>
                                    <MenuItem value={"24"}>Last 24h</MenuItem>
                                    <MenuItem value={"W"}>Last Week</MenuItem>
                                    <MenuItem value={"M"}>Last Month</MenuItem>
                                    <MenuItem value={"C"}>Custom</MenuItem>
                                </Select>
                            </FormControl>
                            {!isCustom? null :
                                <LocalizationProvider dateAdapter={AdapterDayjs}>
                                        <MobileDateTimePicker
                                            disableFuture={true}
                                            sx={{marginLeft: '1em'}}
                                            label="From"
                                            value={from}
                                            onChange={(newValue) => setFrom(newValue)}
                                        />
                                        <MobileDateTimePicker
                                            sx={{marginLeft: '1em'}}
                                            disableFuture={true}
                                            label="To"
                                            value={to}
                                            onChange={(newValue) => setTo(newValue)}
                                        />
                                </LocalizationProvider>
                            }
                            {!isLoading? null :
                                <CircularProgress sx={{marginLeft:'auto'}} />
                            }
                            <Button variant={'contained'}
                                    color={'secondary'}
                                    onClick={getData}
                                    sx={{marginLeft:'auto'}}
                                    size={'large'}>Filter</Button>

                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}
                              alignItems={'center'}
                              justifyContent={'center'}>
                            <Typography variant={'h6'} mr={2}>Units</Typography>
                            <FormControl>
                                <RadioGroup
                                    row
                                    aria-labelledby="units"
                                    defaultValue="C"
                                    value={units}
                                    onChange={handleChange}
                                    name="units-group">
                                    <FormControlLabel value="C" control={<Radio />} label="C" />
                                    <FormControlLabel value="F" control={<Radio />} label="F" />
                                </RadioGroup>
                            </FormControl>
                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}>
                            {isTimeFormatter ?
                            <ResizableBox height={300} width={1100}>
                                <LineChart
                                    series={[
                                        { dataKey:'value',
                                          label: ("Temperature (" + units + ")") as string,
                                            showMark: false
                                        }
                                    ]}
                                    xAxis={[{ scaleType:"time", valueFormatter:timeFormatter, dataKey:'timestamp', label: 'Time'  }]}
                                    dataset={tempData}
                                />
                            </ResizableBox>
                                :
                            <ResizableBox height={300} width={1100}>
                                <LineChart
                                    series={[
                                        { dataKey:'value',
                                            label: ("Temperature (" + units + ")") as string,
                                            showMark: false
                                        }
                                    ]}
                                    xAxis={[{ scaleType:"time", valueFormatter:dateFormatter, dataKey:'timestamp', label: 'Date'  }]}
                                    dataset={tempData}
                                />
                            </ResizableBox>
                        }
                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}>
                            {isTimeFormatter ?
                                <ResizableBox height={300} width={1100}>
                                    <LineChart
                                        series={[
                                            { dataKey:'value',
                                                label: "Humidity (%)",
                                                showMark: false,
                                                color:'#59a14f'
                                            }
                                        ]}
                                        xAxis={[{ scaleType:"time", valueFormatter:timeFormatter, dataKey:'timestamp', label: 'Time',  }]}
                                        dataset={humidityData}
                                    />
                                </ResizableBox>
                                :
                                <ResizableBox height={300} width={1100}>
                                    <LineChart
                                        series={[
                                            { dataKey:'value',
                                                label: "Humidity (%)",
                                                showMark: false,
                                                color:'#59a14f'
                                            }
                                        ]}
                                        xAxis={[{ scaleType:"time", valueFormatter:dateFormatter, dataKey:'timestamp', label: 'Date'  }]}
                                        dataset={humidityData}
                                    />
                                </ResizableBox>
                            }
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
            <PopupMessage message={errorMessage} isSuccess={isSuccess} handleClose={handleErrorPopupClose}
                          open={errorPopupOpen}/>
        </>
    );
}