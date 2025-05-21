import {CommandType} from "./enums/CommandType.ts";
import {CommandParams} from "./CommandParams.ts";
import {WashingMachineCommandParams} from "./WashingMachineCommandParams.ts";

export interface Command {
    deviceId : number,
    commandType: CommandType,
    commandParams : CommandParams | WashingMachineCommandParams
}