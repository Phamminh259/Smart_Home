// @ts-nocheck
import axios from "axios";
import {DeviceDetailsDto} from "../models/DeviceDetailsDto.ts";
import {MeasurementRequest} from "../models/MeasurementRequest.ts";
import {ChartData} from "../models/ChartData.ts";
import EventSource from "react-native-sse";
import {DeviceCapabilities} from "../models/DeviceCapabilities.ts";
import {CommandsDTO} from "../models/CommandsDTO.ts";
import {UserSearchInfo} from "../models/UserSearchInfo.ts";
import {DeviceControlDto} from "../models/DeviceControlDto.ts";
import {PowerMeasurementRequest} from "../models/PowerMeasurementRequest";

export class DeviceService {
    private api_host = "http://localhost:8080"

    public getUserDevices(): Promise<DeviceDetailsDto[]> {
        return axios({
            method: 'GET',
            url: `${this.api_host}/api/devices/ownerAll`,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }

    public getUserSharedDevices(): Promise<DeviceDetailsDto[]> {
        return axios({
            method: 'GET',
            url: `${this.api_host}/api/devices/shared`,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }
    public getDeviceMeasurements(request: MeasurementRequest): Promise<ChartData[]> {
        const result : ChartData[] = [];
        const eventSourceHeader = {headers: { 'Authorization': 'Bearer ' + sessionStorage.getItem('user')}};
        const promise : Promise<ChartData[]> = new Promise((resolve) => {
            const  url = `${this.api_host}/api/devices/measurements?from=${request.from}&to=${request.to}&deviceId=${request.deviceId}&measurement=${request.measurementName}`
            const  es = new EventSource(url, eventSourceHeader);
            es.addEventListener("message", (event) => {
                const arr = JSON.parse(event.data as string)
                if(arr.length < 5000 || (result.length > 0 &&arr[0].timestamp == result[0].timestamp))
                    es.close()
                result.push(...arr)
            });
            es.addEventListener("notice", (event) => {
                console.log(event)

            });
            es.addEventListener("close", () => {
                resolve(result)
            });
        })
        return promise.then((val) => {
            return val})
    }

    public getPowerMeasurements(request: PowerMeasurementRequest): Promise<ChartData[]> {
        const result : ChartData[] = [];
        const eventSourceHeader = {headers: { 'Authorization': 'Bearer ' + sessionStorage.getItem('user')}};
        const promise : Promise<ChartData[]> = new Promise((resolve) => {
            const  url = `${this.api_host}/api/devices/powerMeasurements?from=${request.from}&to=${request.to}&cityId=${request.cityId}&measurement=${request.measurementName}`
            const  es = new EventSource(url, eventSourceHeader);
            es.addEventListener("message", (event) => {
                result.push(...JSON.parse(event.data as string))
                es.close();
            });
            es.addEventListener("close", () => {
                resolve(result)
            });
        })
        return promise.then((val) => {
            return val})
    }

    public getDeviceCapabilities(deviceId: number) : Promise<DeviceCapabilities> {
        return axios({
            method: 'GET',
            url: `${this.api_host}/api/devices/capabilities/` + deviceId,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }

    public getDeviceSharedControl(deviceId: number) : Promise<UserSearchInfo[]> {
        return axios({
            method: 'GET',
            url: `${this.api_host}/api/devices/shareControl/get/${deviceId}`,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }

    public editDeviceSharedControl(deviceId: number, dto: DeviceControlDto) : Promise<string> {
        return axios({
            method: 'PUT',
            url: `${this.api_host}/api/devices/shareControl/${deviceId}`,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
            data: dto
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }

    public getPropertySharedControl(propertyId: number) : Promise<UserSearchInfo[]> {
        return axios({
            method: 'GET',
            url: `${this.api_host}/api/devices/shareControl/get/property/${propertyId}`,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }

    public editPropertySharedControl(propertyId: number, dto: DeviceControlDto) : Promise<string> {
        return axios({
            method: 'PUT',
            url: `${this.api_host}/api/devices/shareControl/property/${propertyId}`,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
            data: dto
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }

    public getPaginatedCommands(deviceId: number, from: number, to: number,page:number, size: number, firstFetch: boolean, userId: number) : Promise<CommandsDTO> {
        return axios({
            method: 'GET',
            url : `${this.api_host}/api/devices/commands?from=${from}&to=${to}&deviceId=${deviceId}&page=${page}&size=${size}&firstFetch=${firstFetch}&userId=${userId}`,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }
}