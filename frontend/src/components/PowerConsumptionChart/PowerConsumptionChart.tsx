// @ts-nocheck
import {
    CircularProgress,
    CssBaseline,
    Grid,
} from "@mui/material";
import {SideNav} from "../Sidenav/SideNav.tsx";
import {UserService} from "../../services/UserService.ts";
import {DeviceService} from "../../services/DeviceService.ts";
import {PopupMessage} from "../PopupMessage/PopupMessage.tsx";
import {ResizableBox} from "../Shared/ResizableBox.tsx";
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';
import React, {useEffect} from "react";
import {MeasurementRequest} from "../../models/MeasurementRequest.ts";
import {LineChart} from "@mui/x-charts";
import {ChartData} from "../../models/ChartData.ts";
import {DataPoint, LTTB} from 'downsample';
import {useNavigate} from "react-router-dom";
import {RoleEnum} from "../../models/enums/RoleEnum";

interface PowerConsumptionChartProps {
    userService: UserService
    deviceService: DeviceService
}

type ChartDataShort = {
    timestamp: Date,
    value: number | null,
}

export function PowerConsumptionChart({userService, deviceService} : PowerConsumptionChartProps) {

    const [errorMessage, setErrorMessage] = React.useState<string>("");
    const [errorPopupOpen, setErrorPopupOpen] = React.useState<boolean>(false);
    const [isSuccess, setIsSuccess] = React.useState(true);
    const [consumptionData, setConsumptionData] = React.useState<ChartDataShort[]>([]);
    const shouldConnect = React.useRef(true);
    const navigate = useNavigate();
    const [latestConsumption, setLatestConsumption] = React.useState<string>("Latest Value: ");
    const [isLoading, setIsLoading] = React.useState<boolean>(true);
    const propertyId = String(location.pathname.split('/').pop());

    const connectSocket = () => {
        try {
            const webChatUrl = "http://localhost:8080/realtime";
            const client = Stomp.over(new SockJS(webChatUrl));

            client.connect(
                {},
                () => {
                    console.log(':::::: SOCKET CONNECTED ::::::');
                    client.subscribe('/consumption/freshest/' + propertyId, onMessageReceived);
                    console.log('test/?');
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
            deviceId: propertyId,
            measurementName: measurement
        }
        deviceService.getDeviceMeasurements(request).then((response => {
            console.log('response is: ' + response)
            if (response.length == 0) {
                return;
            }
            const data : ChartDataShort[] = [];
            response.forEach((value,index) => {
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
            setConsumptionData(downsampled);
            setLatestConsumption("Latest Value: " + downsampled[downsampled.length - 1].value!.toFixed(3) + "kWh");
            setIsLoading(false);
        })).catch((err) => {
            console.log(err);
            setErrorMessage(err.response);
            setIsSuccess(false);
            setErrorPopupOpen(true);
        });
    }

    useEffect(() => {
        if(!shouldConnect.current) return;
        connectSocket();
        getMeasurement("totalConsumption");
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

    const [ordinalPowerConsumption, setOrdinalPowerConsumption] = React.useState<number>(0);
    const onMessageReceived = (payload) => {
        const timestamp = new Date();
        console.log('test2');
        const val: ChartData = JSON.parse(payload.body);
        const newVal: ChartDataShort = {
            timestamp: new Date(+val.timestamp),
            value: val.value,
        };
        console.log('test3');
        console.log(ordinalPowerConsumption + ". " + timestamp);
        setOrdinalPowerConsumption(ordinalPowerConsumption + 1);
        setLatestConsumption("Latest Value: " + newVal.value!.toFixed(3) + "kWh")
        setConsumptionData((prevConsumptionData) => {
            console.log(prevConsumptionData.length + " " + newVal.timestamp.getTime() + " " + prevConsumptionData[prevConsumptionData.length - 1].timestamp.getTime() + " " + (newVal.timestamp.getTime() - prevConsumptionData[prevConsumptionData.length - 1].timestamp.getTime()));
            if (prevConsumptionData.length > 0) {
                if (newVal.timestamp.getTime() - prevConsumptionData[0].timestamp.getTime() > 3900000)
                    prevConsumptionData.shift();
                if (newVal.timestamp.getTime() - prevConsumptionData[prevConsumptionData.length - 1].timestamp.getTime() > 720000) {
                    const nullVal: ChartDataShort = {
                        timestamp: new Date(val.timestamp),
                        value: null
                    }
                    prevConsumptionData.push(nullVal)
                }
            }

            return [...prevConsumptionData, newVal];
        });
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
                        {!isLoading? null :
                            <CircularProgress sx={{position:'absolute',right:'20px'}} />
                        }
                        <Grid container item xs={12} sm={12} md={12} lg={12} xl={12}>
                            <ResizableBox height={300} width={1100}>
                                <LineChart
                                    series={[
                                        { dataKey:'value', showMark: false, label: ('Power consumption (kWh)\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0' + latestConsumption) as string, color:'#59a14f' },
                                    ]}
                                    xAxis={[{ scaleType:'time', dataKey:'timestamp', label:'Time' }]}
                                    dataset={consumptionData}
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