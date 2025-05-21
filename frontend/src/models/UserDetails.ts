import {RoleEnum} from "./enums/RoleEnum.ts";

export interface UserDetails {
    id: number,
    username: string,
    email: string,
    profilePicture: string
    role: RoleEnum
}