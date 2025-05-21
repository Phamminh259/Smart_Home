import {RoleEnum} from "./enums/RoleEnum.ts";

export interface NewUserMultipart {
    username: string,
    name: string,
    surname: string,
    email: string,
    password: string,
    profilePicture: File
    role: RoleEnum
}