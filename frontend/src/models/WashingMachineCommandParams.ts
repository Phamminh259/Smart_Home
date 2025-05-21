import {WashingMachineMode} from "./enums/WashingMachineMode.ts";

export interface WashingMachineCommandParams {
    userId: number,
    unit: string,
    centrifugeSpeed: number,
    temp: number,
    mode: WashingMachineMode,
    from: number,
    taskId: number
}