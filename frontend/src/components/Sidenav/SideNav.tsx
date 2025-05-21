// @ts-nocheck
import React from "react";
import {
    AppBar,
    Box,
    Drawer,
    Grid,
    IconButton,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText, Paper,
    Toolbar,
    Typography
} from "@mui/material";
import HomeIcon from '@mui/icons-material/Home';
import LogoutIcon from '@mui/icons-material/Logout';
import MenuIcon from '@mui/icons-material/Menu';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import DeviceThermostatIcon from '@mui/icons-material/DeviceThermostat';
import './SideNav.css'
import {Link, useLocation, useNavigate} from "react-router-dom";
import {UserService} from "../../services/UserService";
import AddHomeIcon from '@mui/icons-material/AddHome';
import {House, HouseSiding, LocationCity} from "@mui/icons-material";

interface SideNavProps {
    userService: UserService
    isAdmin: boolean,
    isSuperadmin: boolean
}

export function SideNav({userService, isSuperadmin, isAdmin} : SideNavProps) {
    const [mobileOpen, setMobileOpen] = React.useState(false);
    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };
    const container = window !== undefined ? () => window.document.body : undefined;
    const navigate = useNavigate();
    const {pathname} = useLocation();

    const drawer = (
        <Paper className={'dark-background height-100 white-text nav-container'}>
            <Toolbar/>
            <img className={'rounded-avatar-picture'}
                 src={sessionStorage.getItem('profilePicture') as string}
                 alt={'Profile picture'}/>
            <br/>
            <Typography variant={'h4'} fontWeight={'lighter'}>{sessionStorage.getItem('username')}</Typography>
            <br/>
            <br/>
            <br/>
            <List>
                <Link to={isAdmin? '/adminMain' : '/userMain' } className={'white-text'}>
                    <ListItem key={isAdmin? '/adminMain' : '/userMain' } disablePadding selected={pathname.includes('Main')}>
                        <ListItemButton>
                            <ListItemIcon><HomeIcon className={'white-text'}/></ListItemIcon>
                            <ListItemText primary={isAdmin? "Requests" : "Property"}/>
                        </ListItemButton>
                    </ListItem>
                </Link>
                {isSuperadmin?
                    <Link to={'/newAdmin'} className={'white-text'}>
                        <ListItem key='/newAdmin' disablePadding selected={pathname.includes('Admin')}>
                            <ListItemButton>
                                <ListItemIcon><PersonAddIcon className={'white-text'}/></ListItemIcon>
                                <ListItemText primary={'Add admin'}/>
                            </ListItemButton>
                        </ListItem>
                    </Link>
                    :
                    null
                }
                {(isAdmin || isSuperadmin) ? null :
                    <Link to={'/userRegisterProperty'} className={'white-text'}>
                        <ListItem key='/userRegisterProperty' disablePadding selected={pathname.includes('RegisterProperty')}>
                            <ListItemButton>
                                <ListItemIcon><AddHomeIcon className={'white-text'}/></ListItemIcon>
                                <ListItemText primary='Register Property'/>
                            </ListItemButton>
                        </ListItem>
                    </Link>
                }
                {(isAdmin || isSuperadmin) ? null :
                    <Link to={'/devices'} className={'white-text'}>
                        <ListItem key='/devices' disablePadding selected={pathname.includes('devices')}>
                            <ListItemButton>
                                <ListItemIcon><DeviceThermostatIcon className={'white-text'}/></ListItemIcon>
                                <ListItemText primary='Devices'/>
                            </ListItemButton>
                        </ListItem>
                    </Link>
                }
                {(isAdmin || isSuperadmin) ? <Link to={'/properties'} className={'white-text'}>
                    <ListItem key='/properties' disablePadding selected={pathname.includes('properties')}>
                        <ListItemButton>
                            <ListItemIcon><House className={'white-text'}/></ListItemIcon>
                            <ListItemText primary='Properties overlook'/>
                        </ListItemButton>
                    </ListItem>
                </Link> :
                    null
                }

                {(isAdmin || isSuperadmin) ? <Link to={'/cityPowerOverlook'} className={'white-text'}>
                        <ListItem key='/cityPowerOverlook' disablePadding selected={pathname.includes('cityPowerOverlook')}>
                            <ListItemButton>
                                <ListItemIcon><LocationCity className={'white-text'}/></ListItemIcon>
                                <ListItemText primary='City power overlook'/>
                            </ListItemButton>
                        </ListItem>
                    </Link> :
                    null
                }

                <ListItem key='sign-out' disablePadding className={'align-bottom center-items width-exact'}>
                    <ListItemButton onClick={() => {
                        userService.signOut();
                        navigate("/")
                    }}>
                        <ListItemIcon><LogoutIcon className={'white-text'}/></ListItemIcon>
                        <ListItemText primary='Sign Out'/>
                    </ListItemButton>
                </ListItem>
            </List>
        </Paper>
    );


    return (
        <Grid container style={{borderRight:'none'}} className={'dark-background'}>
            <AppBar
                sx={{
                    width: {xs: '100%', sm: '100%', xl: '0px', md: '0px', lg: '0px', xxl: '0px'},
                    color:'white'
                }}>
                <Toolbar className={'dark-background'}>
                    <IconButton
                        color="inherit"
                        aria-label="open drawer"
                        edge="start"
                        onClick={handleDrawerToggle}
                        sx={{mr: 2}}>
                        <MenuIcon/>
                    </IconButton>
                </Toolbar>
            </AppBar>
            <Grid item xl={3} lg={3} md={3} sm={10} xs={12}>
                <Box
                    className={'dark-background'}
                    component="div"
                    sx={{
                        width: {
                            width: {xl: '20%', lg: '20%', md: '25%', sm: '40%', xs: '70%'},
                            border: 'none'
                        }
                    }}
                    aria-label="navbar">
                    <Drawer
                        container={container}
                        variant="temporary"
                        open={mobileOpen}
                        style={{backgroundColor:'transparent'}}
                        onClose={handleDrawerToggle}
                        ModalProps={{
                            keepMounted: true,
                        }}
                        sx={{
                            display: {xs: 'block', sm: 'block'},
                            '& .MuiDrawer-paper': {width: {xl: '20%', lg: '20%', md: '25%', sm: '40%', xs: '70%'},backgroundColor:'#0f1924',
                                borderRight:0, borderBottomRightRadius:'1em', borderTopRightRadius:'1em'},
                        }}>
                        {drawer}
                    </Drawer>
                    <Drawer
                        variant="permanent"
                        sx={{
                            display: {xs: 'none', sm: 'none', md: 'block'},
                            '& .MuiDrawer-paper': {width: {xl: '20%', lg: '20%', md: '25%', sm: '40%', xs: '70%',borderRight:0,
                                backgroundColor: '#0f1924'}},
                        }}
                        open>
                        {drawer}
                    </Drawer>
                </Box>
            </Grid>
        </Grid>
    );
}