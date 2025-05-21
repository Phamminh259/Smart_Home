import {PowerSource} from "./enums/PowerSource.ts";

export interface DeviceDetailsDto {
    id: number,
    type: string,
    name: string,
    powerSource: PowerSource,
    energyConsumption: number,
    picture: string,
    propertyName: string
}