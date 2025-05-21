// @ts-nocheck
import {
    Button,
    CssBaseline,
    Grid,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow
} from "@mui/material";
import {SideNav} from "../Sidenav/SideNav.tsx";
import {UserService} from "../../services/UserService.ts";
import {RoleEnum} from "../../models/enums/RoleEnum.ts";
import {PropertyService} from "../../services/PropertyService";
import React, {useEffect, useRef} from "react";
import {Property} from "../../models/Property";
import {useNavigate} from "react-router";
import {PopupMessage} from "../PopupMessage/PopupMessage";

/*interface AdminMainProps {
    userService: UserService,
}
function createData(
    address: string,
    city: string,
    size: string,
    floors: string,
    status: string,
) {
    return { address, city, size, floors, status };
}*/
interface PropertyProps {
    userService: UserService,
    propertyService: PropertyService
}

export function AdminMain({userService, propertyService} : PropertyProps) {
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = React.useState<string>("");
    const [errorPopupOpen, setErrorPopupOpen] = React.useState<boolean>(false);
    const [isSuccess, setIsSuccess] = React.useState(true);
    const shouldLoad = useRef(true);
    const [properties, setProperty] = React.useState<Property[]>([]);

    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);
    };

    const getUserProperties = async () => {
        try {
            const response = await propertyService.getAllUnapprovedProperty();
            if (response.length > 0) {
                setProperty(response);
            }
        } catch (err) {
            setErrorMessage(err.response?.data || "An error occurred while fetching properties.");
            setIsSuccess(false);
            setErrorPopupOpen(true);
        }
    };
    useEffect(() => {
        const fetchUserProperties = async () => {
            try {
                await getUserProperties();
                shouldLoad.current = false;
            } catch (err) {
                console.error("Error fetching properties:", err);
            }
        };

        if (shouldLoad.current) {
            fetchUserProperties();
        }
    }, [propertyService, getUserProperties]);

    useEffect(() => {
        if (sessionStorage.getItem("expiration") != null) {
            setTimeout(() => {
                setErrorMessage("Session expired. Please log in again.");
                setIsSuccess(false);
                setErrorPopupOpen(true);
                setTimeout(() => navigate("/"), 5000);
            }, Number(sessionStorage.getItem("expiration")) - Date.now())
        } else {
            navigate("/")
        }
    });

    const approveProperty = async (propertyId: number) => {
        try {
            await propertyService.approveProperty(propertyId);
            window.location.reload();
            setErrorMessage("Property approved!");
            setIsSuccess(true);
            setErrorPopupOpen(true);
        } catch (err) {
            setErrorMessage(err.response?.data || "An error occurred while approving property.");
            setIsSuccess(false);
            setErrorPopupOpen(true);
        }
    };


    const handleApprovedClick = (event: React.MouseEvent<HTMLElement>,propertyId: number) => {
        approveProperty(propertyId);
    };

    const denyProperty = async (propertyId: number) => {
        try {
            await propertyService.denyProperty(propertyId);
            window.location.reload();
            setErrorMessage("Property denied!");
            setIsSuccess(true);
            setErrorPopupOpen(true);
        } catch (err) {
            setErrorMessage(err.response?.data || "An error occurred while denying property.");
            setIsSuccess(false);
            setErrorPopupOpen(true);
        }
    };

    const handleDeniedClick = (event: React.MouseEvent<HTMLElement>,propertyId: number) => {
        denyProperty(propertyId);
    };

    return (
        <>
            <CssBaseline/>
            <Grid container className={'dark-background'} height={'100%'}  justifyContent={'flex-start'}>
                <Grid item xs={0} sm={0} md={2} lg={2} xl={2}>
                    <SideNav userService={userService} isAdmin={sessionStorage.getItem("role") == RoleEnum.ROLE_ADMIN ||
                        sessionStorage.getItem("role") == RoleEnum.ROLE_SUPERADMIN}
                             isSuperadmin={sessionStorage.getItem("role") == RoleEnum.ROLE_SUPERADMIN}/>
                </Grid>
                <Grid item height={'100%'}  xl={10} lg={10} md={10} sm={12} xs={12}
                      p={2}
                      className={'white-background'}
                      style={{borderRadius:'1.5em', overflowY:'scroll'}}
                      alignItems={'flex-start'}
                      ml={{xl: '20%', lg: '20%', md: '25%', sm: '0', xs: '0'}}
                      mt={{xl: 0, lg: 0, md: 0, sm: '64px', xs: '64px'}}>
                    <TableContainer component={Paper}>
                        <Table sx={{ minWidth: 650 }} aria-label="simple table">
                            <TableHead>
                                <TableRow>
                                    <TableCell align="center">Địa chỉ</TableCell>
                                    <TableCell align="center">Giấy phép</TableCell>
                                    <TableCell align="center">Độ rộng</TableCell>
                                    <TableCell align="center">Số tầng</TableCell>
                                    <TableCell align="center">Trạng Thái</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {properties.map((row) => (
                                    <TableRow
                                        key={row.id}
                                        sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                                    >
                                        <TableCell component="th" scope="row">
                                            {row.address}
                                        </TableCell>
                                        <TableCell align="center">{row.city}</TableCell>
                                        <TableCell align="center">{row.size}</TableCell>
                                        <TableCell align="center">{row.floors}</TableCell>
                                        <TableCell align="center">
                                            <Button variant="contained" color="primary" component="span"
                                                    onClick={(evt) => handleApprovedClick(evt, row.id)}>
                                            Approve
                                            </Button>
                                            <Button variant="contained" color="secondary" component="span"
                                                    onClick={(evt) => handleDeniedClick(evt, row.id)}>
                                                Deny
                                            </Button>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Grid>
            </Grid>
            <PopupMessage message={errorMessage} isSuccess={isSuccess} handleClose={handleErrorPopupClose} open={errorPopupOpen}/>
        </>
    );
}