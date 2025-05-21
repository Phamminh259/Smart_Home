import {ACMode} from "./enums/ACMode.ts";

export interface CommandParams {
    userId: number,
    unit: string,
    target: number,
    fanSpeed: number,
    currentTemp: number,
    health: boolean,
    fungus: boolean,
    mode: ACMode,
    everyDay: boolean,
    from: number,
    to: number,
    taskId: number
}