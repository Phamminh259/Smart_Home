// @ts-nocheck
import {
    CircularProgress,
    CssBaseline,
    FormControl,
    FormControlLabel,
    Grid,
    Radio,
    RadioGroup,
    Typography
} from "@mui/material";
import {SideNav} from "../Sidenav/SideNav.tsx";
import {UserService} from "../../services/UserService.ts";
import {DeviceService} from "../../services/DeviceService.ts";
import {PopupMessage} from "../PopupMessage/PopupMessage.tsx";
import {ResizableBox} from "../Shared/ResizableBox.tsx";
import Stomp, {Message} from "stompjs";
import SockJS from 'sockjs-client';
import React, {useEffect} from "react";
import {MeasurementRequest} from "../../models/MeasurementRequest.ts";
import {LineChart} from "@mui/x-charts";
import {ChartData} from "../../models/ChartData.ts";
import {DataPoint, LTTB} from 'downsample';
import {useNavigate} from "react-router-dom";
import {RoleEnum} from "../../models/enums/RoleEnum";

interface ThermometerChartsProps {
    userService: UserService
    deviceService: DeviceService
}

type ChartDataShort = {
    timestamp: Date,
    value: number | null,
}

export function ThermometerCharts({userService, deviceService} : ThermometerChartsProps) {

    const [errorMessage, setErrorMessage] = React.useState<string>("");
    const [errorPopupOpen, setErrorPopupOpen] = React.useState<boolean>(false);
    const [isSuccess, setIsSuccess] = React.useState(true);
    const [tempData, setTempData] = React.useState<ChartDataShort[]>([]);
    const [humidityData, setHumidityData] = React.useState<ChartDataShort[]>([]);
    const deviceId = String(location.pathname.split('/').pop());
    const shouldConnect = React.useRef(true);
    const navigate = useNavigate();
    const units = React.useRef("C");
    const [unitA, setUnitA] = React.useState<string>("C");
    const [latestTemp, setLatestTemp] = React.useState<string>("Latest Value: ");
    const [latestHum, setLatestHum] = React.useState<string>("Latest Value: ");
    const [isLoading, setIsLoading] = React.useState<boolean>(true);
    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        changeUnit((event.target as HTMLInputElement).value);
        units.current = (event.target as HTMLInputElement).value;
        setUnitA((event.target as HTMLInputElement).value);
    };

    const connectSocket = () => {
        try {
            const webChatUrl = "http://localhost:8080/realtime";
            const client = Stomp.over(new SockJS(webChatUrl));

            client.connect(
                {},
                () => {
                    console.log(':::::: SOCKET CONNECTED ::::::');
                    client.subscribe('/thermometer/freshest/' + deviceId, onMessageReceived);
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

    /*const insertNulls = (data: ChartDataShort[]) : ChartDataShort[] => {
        const result : ChartDataShort[] = [];
        data.forEach((val,idx) => {
            if(idx!= data.length - 1 && data.length >= 2 &&
                (data[idx + 1].timestamp.getTime() - val.timestamp.getTime() > 60000)){
                const nullVal : ChartDataShort = {
                    timestamp: val.timestamp,
                    value: null
                }
                result.push(nullVal);
            }
            result.push(val);
        });
        return result;
    }*/


    const changeUnit = (unit : string) => {
        if(units.current == unit) return;
        const transformedData: ChartDataShort[] = [];
        if(unit == "F") {
            tempData.forEach((val) => {
                if(val.value == null) {
                    transformedData.push(val);
                }
                else {
                    const transformed: ChartDataShort = {
                        timestamp: val.timestamp,
                        value: (val.value * 9 / 5) + 32
                    }
                    transformedData.push(transformed)
                }
            })
        }
        else {
            tempData.forEach((val) => {
                if(val.value == null) {
                    transformedData.push(val);
                }
                else {
                    const transformed: ChartDataShort = {
                        timestamp: val.timestamp,
                        value: (val.value - 32) * 5 / 9
                    }
                    transformedData.push(transformed)
                }
            })
        }
        setTempData(transformedData);
        units.current = unit;
        setLatestTemp("Latest Value: " + transformedData[transformedData.length - 1].value!.toFixed(3) + units.current)
    }
    const downsample = (data: ChartDataShort[], targetLength : number) => {
        const dataPoints : DataPoint[] = [];
        data.forEach((val) => {
            if(val.value != null) {
                const point: DataPoint = {
                    x: val.timestamp,
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
        //const r : ChartDataShort[] = insertNulls(result);
        return result;
    }

    const getMeasurement = (measurement: string) => {
        setIsLoading(true);
        const now = new Date()
        const from = now.getTime() - 1000 * 60 * 60;
        const request : MeasurementRequest = {
            from: Math.floor(from / 1000),
            to: Math.floor(Date.now() / 1000),
            deviceId: deviceId,
            measurementName: measurement
        }
        deviceService.getDeviceMeasurements(request).then(response => {
            console.log(response)
            if (response.length == 0) {
                setIsLoading(false);
                return;
            }
            const data : ChartDataShort[] = [];
            response.forEach((value) => {
                const newVal : ChartDataShort = {
                    timestamp : new Date(value.timestamp),
                    value: value.value,
                };
                data.push(newVal);
            })
            let downsampled : ChartDataShort[] = data;
            console.log('data length: ' + data.length);
            if(data.length > 60)
                downsampled = downsample(data,60);
            if(measurement == "temperature") {
               setTempData(downsampled);
               setUnitA(response[0].tags['unit']!)
               units.current = response[0].tags['unit']!
               setLatestTemp("Latest Value: " + downsampled[downsampled.length - 1].value!.toFixed(3) + units.current)
            }
            else {
               setHumidityData(downsampled);
               setLatestHum("Latest Value: " + downsampled[downsampled.length - 1].value!.toFixed(3) + "%")
            }
            setIsLoading(false);
        }).catch((err) => {
            console.log(err);
            setErrorMessage(err.response);
            setIsSuccess(false);
            setErrorPopupOpen(true);
        });
    }

    useEffect(() => {
        if(!shouldConnect.current) return;
        connectSocket();
        getMeasurement("temperature");
        getMeasurement("humidity");
        shouldConnect.current = false;
    }, []);

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
    const ordinalTemp = React.useRef(1);
    const ordinalHum = React.useRef(1);
    const onMessageReceived = (payload : Message) => {
        const timestamp = new Date();
        const val : ChartData =  JSON.parse(payload.body);
        const newVal : ChartDataShort = {
            timestamp : new Date(+val.timestamp),
            value: val.value,
        };
        if(val.name == "temperature"){
            console.log("Received TEMPERATURE (" + ordinalTemp.current + " - " + timestamp);
            ordinalTemp.current += 1;
            if(val.tags['unit'] != units.current) {
                if(val.tags['unit'] == "C")
                    newVal.value = (val.value * 9/5) + 32
                else
                    newVal.value = (val.value - 32) * 5 / 9
            }
            setLatestTemp("Latest Value: " + newVal.value!.toFixed(3) + units.current)
            setTempData((prevTempData) => {
                if(prevTempData.length > 0 ) {
                    if (newVal.timestamp.getTime() - prevTempData[0].timestamp.getTime() > 3900000)
                        prevTempData.shift();

                    if (prevTempData.length == 0 || newVal.timestamp.getTime() - prevTempData[prevTempData.length - 1].timestamp.getTime() > 60000) {
                        const nullVal: ChartDataShort = {
                            timestamp: new Date(val.timestamp),
                            value: null
                        }
                        prevTempData.push(nullVal)
                    }
                }

                return [...prevTempData, newVal];
            });
        }
        else {
            console.log("Received HUMIDITY (" + ordinalHum.current + " - " + timestamp);
            ordinalHum.current += 1;
            setLatestHum("Latest Value: " + newVal.value!.toFixed(3) + "%")
            setHumidityData((prevHumData) => {
                console.log(prevHumData.length + " " + newVal.timestamp.getTime() + " " + prevHumData[prevHumData.length - 1].timestamp.getTime() + " " + (newVal.timestamp.getTime() - prevHumData[prevHumData.length - 1].timestamp.getTime()));
                if(prevHumData.length > 0 ) {
                    if (newVal.timestamp.getTime() - prevHumData[0].timestamp.getTime() > 3900000)
                        prevHumData.shift();
                    if (newVal.timestamp.getTime() - prevHumData[prevHumData.length - 1].timestamp.getTime() > 60000)
                    {
                        const nullVal : ChartDataShort = {
                            timestamp: new Date(val.timestamp),
                            value: null
                        }
                        prevHumData.push(nullVal)
                    }
                }

                return [...prevHumData, newVal];
            });
        }
    }


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
                              justifyContent={'center'}>
                            <Typography variant={'h6'} mr={2}>Units</Typography>
                            <FormControl>
                                <RadioGroup
                                    row
                                    aria-labelledby="units"
                                    defaultValue="C"
                                    value={unitA}
                                    onChange={handleChange}
                                    name="units-group">
                                    <FormControlLabel value="C" control={<Radio />} label="C" />
                                    <FormControlLabel value="F" control={<Radio />} label="F" />
                                </RadioGroup>
                            </FormControl>

                        </Grid>
                        {!isLoading? null :
                            <CircularProgress sx={{position:'absolute',right:'20px'}} />
                        }
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}>
                            <ResizableBox height={300} width={1100}>
                                <LineChart
                                    series={[
                                        { dataKey:'value', showMark: false, label: ("Temperature (" + unitA + ")\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0" + latestTemp) as string},
                                    ]}
                                    xAxis={[{ scaleType:'time', dataKey:'timestamp', label: 'Time'  }]}

                                    dataset={tempData}
                                />
                            </ResizableBox>
                        </Grid>
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}>
                            <ResizableBox height={300} width={1100}>
                                <LineChart
                                    series={[
                                        { dataKey:'value', showMark: false, label: ('Humidity (%)\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0' + latestHum), color:'#59a14f' },
                                    ]}
                                    xAxis={[{ scaleType:'time', dataKey:'timestamp', label:'Time' }]}
                                    dataset={humidityData}
                                />
                            </ResizableBox>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
            <PopupMessage message={errorMessage} isSuccess={isSuccess} handleClose={handleErrorPopupClose}
                          open={errorPopupOpen}/>
        </>
    );
}