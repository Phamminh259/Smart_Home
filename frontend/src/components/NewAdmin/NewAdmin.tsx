// @ts-nocheck
import {UserService} from "../../services/UserService.ts";
import {Registration} from "../Registration/Registration.tsx";
import {CssBaseline, Grid} from "@mui/material";
import {SideNav} from "../Sidenav/SideNav.tsx";
import {RoleEnum} from "../../models/enums/RoleEnum";

interface NewAdminProps {
    userService: UserService
}

export function NewAdmin({userService} : NewAdminProps) {
    return (
        <>
            <CssBaseline/>
            <Grid container className={'dark-background'} height={'100%'}  justifyContent={'flex-start'}>
                <Grid item xs={0} sm={0} md={2} lg={2} xl={2}>
                    <SideNav userService={userService} isAdmin={sessionStorage.getItem("role") == RoleEnum.ROLE_ADMIN ||
                        sessionStorage.getItem("role") == RoleEnum.ROLE_SUPERADMIN} isSuperadmin={sessionStorage.getItem("role") == RoleEnum.ROLE_SUPERADMIN}/>
                </Grid>
                <Grid container item height={'100%'}  xl={10} lg={10} md={10} sm={12} xs={12}
                      p={2}
                      className={'white-background'}
                      style={{borderRadius:'1.5em', overflowY:'scroll'}}
                      alignItems={'center'}
                      mt={{xl: 0, lg: 0, md: 0, sm: '64px', xs: '64px'}}>
                    <Grid item>
                        <Registration userService={userService} adminRegistration={true}/>
                    </Grid>
                </Grid>
            </Grid>
        </>
    );
}