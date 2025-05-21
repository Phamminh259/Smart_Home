import './App.css'
import {UserService} from "./services/UserService";
import {createTheme, ThemeProvider} from "@mui/material";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import {Login} from "./components/Login/Login";
import {AdminMain} from "./components/AdminMain/AdminMain.tsx";
import {UserMain} from "./components/UserMain/UserMain.tsx";
import {NewAdmin} from "./components/NewAdmin/NewAdmin.tsx";
import {UserRegisterProperty} from "./components/UserRegisterProperty/UserRegisterProperty";

import {PropertyService} from "./services/PropertyService";
import {UserRegisterDevice} from "./components/UserRegisterDevice/UserRegisterDevice";
import {PasswordReset} from "./components/PasswordReset/PasswordReset.tsx";
import {UserDevices} from "./components/UserDevices/UserDevices.tsx";
import {DeviceService} from "./services/DeviceService.ts";
import {ThermometerCharts} from "./components/ThermometerCharts/ThermometerCharts.tsx";
import {ThermometerChartsHistory} from "./components/ThermometerHistoryCharts/ThermometerHistoryCharts.tsx";
import {ACCommandsReport} from "./components/ACCommandsReport/ACCommandsReport.tsx";
import {PowerConsumptionChart} from "./components/PowerConsumptionChart/PowerConsumptionChart";
import {PowerConsumptionChartsHistory} from "./components/PowerConsumptionHistoryChart/PowerConsumptionHistoryCharts";
import {WebsocketsSimulator} from "./components/WebsocketsSimulator/WebsocketsSimulator.tsx";
import {AdminProperties} from "./components/AdminProperties/AdminProperties";
import {PowerProductionChart} from "./components/PowerProductionChart/PowerProductionChart";
import {PowerProductionChartsHistory} from "./components/PowerProductionHistoryChart/PowerProductionHistoryCharts";
import {CityPowerOverlook} from "./components/CityPowerOverlook/CityPowerOverlook";
import {UnifyingPowerCharts} from "./components/UnifyingPowerChart/UnifyingPowerCharts";



function App() {
    const userServiceSingleton = new UserService();
    const propertyServiceSingleton = new PropertyService();
    const deviceServiceSingleton = new DeviceService();

    const newColorScheme = createTheme({
        palette: {
            primary: {
                main: '#00ADB5'
            },
            secondary: {
                main: '#FF5722'
            }
        },
        components: {
            MuiButton: {
                styleOverrides: {
                    root: {
                        color:'white',
                        fontWeight:'400',
                        borderRadius:'0.6em'
                    }
                }
            },
            MuiFab: {
                styleOverrides: {
                    root: {
                        color:'white'
                    }
                }
            },
            MuiInputBase: {
                styleOverrides: {
                    root: {
                        borderRadius:'1em !important'
                    }
                }
            },
            MuiImageListItem: {
                styleOverrides: {
                    root: {
                        fontWeight:'300'
                    }
                }
            },
            MuiStepIcon: {
                styleOverrides: {
                    text: {
                        fill:'white'
                    }
                }
            },
            MuiStepLabel: {
                styleOverrides: {
                    root: {
                        fontWeight:'300',
                    },

                }
            },
            MuiDialog: {
                styleOverrides: {
                    paper: {
                        borderRadius:'1em'
                    }
                }
            },
            MuiDialogActions: {
                styleOverrides: {
                    root: {
                        paddingRight:'1.5em',
                        paddingBottom:'1em'
                    }
                }
            },

            MuiDrawer: {
                styleOverrides: {
                    root: {
                        "& .Mui-selected": {
                            backgroundColor: 'rgba(0,173, 181,0.2)',
                            width:'95%',
                            borderTopRightRadius:'1.5em',
                            borderBottomRightRadius:'1.5em',
                        },
                        "& .Mui-selected:hover": {
                            backgroundColor: 'rgba(0,173, 181,0.4)'
                        }
                    }
                }
            }
        }
    });

    return (
        <>
            <ThemeProvider theme={newColorScheme}>
                <BrowserRouter>
                    <Routes>
                        <Route path="/" element={<Login userService={userServiceSingleton}/>}/>
                        <Route path="/sim" element={<WebsocketsSimulator/>}/>
                        <Route path="/adminMain" element={<AdminMain userService={userServiceSingleton} propertyService={propertyServiceSingleton}/>}/>
                        <Route path="/userMain" element={<UserMain userService={userServiceSingleton} deviceService={deviceServiceSingleton} propertyService={propertyServiceSingleton}/>}/>
                        <Route path="/properties" element={<AdminProperties userService={userServiceSingleton} deviceService={deviceServiceSingleton} propertyService={propertyServiceSingleton}/>}/>
                        <Route path="/newAdmin" element={<NewAdmin userService={userServiceSingleton}/>}/>
                        <Route path="/userRegisterProperty" element={<UserRegisterProperty userService={userServiceSingleton} propertyService={propertyServiceSingleton}/>}/>
                        <Route path="/userRegisterDevice/:propertyId" element={<UserRegisterDevice userService={userServiceSingleton}/>}/>
                        <Route path="/userRegisterDevice" element={<UserRegisterDevice userService={userServiceSingleton}/>}/>
                        <Route path="/passwordReset/:id" element={<PasswordReset userService={userServiceSingleton}/>}/>
                        <Route path="/devices" element={<UserDevices userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                        <Route path="/thermoCharts/:id" element={<ThermometerCharts userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                        <Route path="/thermoChartsHistory/:id" element={<ThermometerChartsHistory userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                        <Route path="/consumptionCharts/:id" element={<PowerConsumptionChart userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                        <Route path="/consumptionChartsHistory/:id" element={<PowerConsumptionChartsHistory userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                        <Route path="/productionCharts/:id" element={<PowerProductionChart userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                        <Route path="/productionChartsHistory/:id" element={<PowerProductionChartsHistory userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                        <Route path="/acCommands/:id" element={<ACCommandsReport userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                        <Route path="/cityPowerOverlook/:id" element={<CityPowerOverlook userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                        <Route path="/cityPowerOverlook" element={<CityPowerOverlook userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                        <Route path="/unifyingPowerCharts/:id" element={<UnifyingPowerCharts userService={userServiceSingleton} deviceService={deviceServiceSingleton}/>}/>
                    </Routes>
                </BrowserRouter>
            </ThemeProvider>
        </>
    )

}

export default App
