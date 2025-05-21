import {CommandSummary} from "./CommandSummary.ts";
import {UserIdUsernamePair} from "./UserIdUsernamePair.ts";

export interface CommandsDTO {
    commands: CommandSummary[],
    allUsers: UserIdUsernamePair[]
}