
import {
    Autocomplete,
    Button, CircularProgress,
    CssBaseline,
    Grid,
    IconButton,
    TextField,
    Typography
} from "@mui/material";
import {SideNav} from "../Sidenav/SideNav.tsx";
import {UserService} from "../../services/UserService.ts";
import {DeviceService} from "../../services/DeviceService.ts";
import {DataGrid, GridColDef, GridPaginationModel, GridValueGetterParams} from "@mui/x-data-grid";
import {CommandSummary} from "../../models/CommandSummary.ts";
import React, {useEffect, useRef} from "react";
import {LocalizationProvider, MobileDateTimePicker} from "@mui/x-date-pickers";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {Dayjs} from "dayjs";
import {PopupMessage} from "../PopupMessage/PopupMessage.tsx";
import {useNavigate} from "react-router-dom";
import {UserIdUsernamePair} from "../../models/UserIdUsernamePair.ts";
import CloseIcon from "@mui/icons-material/Close";
import {RoleEnum} from "../../models/enums/RoleEnum";

interface ACCommandsReportProps {
    userService: UserService,
    deviceService: DeviceService
}

function getDate(params: GridValueGetterParams) {
    return `${new Date(params.row.timestamp).toLocaleString()}`;
}

const columns: GridColDef[] = [
    { field: 'timestamp',
        headerName: 'Date & Time',
        type:'Date',
        minWidth: 300,
        valueGetter: getDate,
        align:'center',
        headerAlign:'center',
    },
    { field: 'username',
        headerName: 'User',
        type:'string',
        minWidth: 300,
        headerClassName: 'bold-and-colored',
        cellClassName: 'bold-and-colored',
        align:'center',
        headerAlign:'center',
    },
    { field: 'command',
        headerName: 'Command',
        type:'string',
        flex: 2,
        align:'center',
        headerAlign:'center' }
];
export function ACCommandsReport({userService, deviceService} : ACCommandsReportProps) {
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = React.useState<string>("");
    const [errorPopupOpen, setErrorPopupOpen] = React.useState<boolean>(false);
    const [isSuccess, setIsSuccess] = React.useState(true);
    const deviceId = String(location.pathname.split('/').pop());
    const [commands,setCommands] = React.useState<CommandSummary[]>([]);
    const [currentPageCommands,setCurrentPageCommands] = React.useState<CommandSummary[]>([]);
    const [from, setFrom] = React.useState<Dayjs | null>(null);
    const [usernames, setUsernames] = React.useState<UserIdUsernamePair[]>([]);
    const [to, setTo] = React.useState<Dayjs | null>(null);
    const firstLoad = useRef(true);
    const [isLoading, setIsLoading] = React.useState<boolean>(true);
    const [page, setPage] = React.useState<number>(0);
    const [rowCount, setRowCount] = React.useState<number>(100);
    const [prevPage, setPrevPage] = React.useState<number>(0);
    const [prevPageSize, setPrevPageSize] = React.useState<number>(10);
    const [user, setUser] = React.useState<UserIdUsernamePair | null>(null);
    const [inputValueUser, setInputValueUser] = React.useState('');
    const [isFilteredData, setIsFilteredData] = React.useState<boolean>(false);
    const [filteredCommands, setFilteredCommands] = React.useState<CommandSummary[]>([]);
    const hasAllLoaded = React.useRef(false);
    const [isLoadingOutside, setIsLoadingOutside] = React.useState<boolean>(false);
    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);
    };

    const resetFrom = () => setFrom(null);
    const resetTo = () => setTo(null);

    const filter = () => {
        hasAllLoaded.current = false;
        setIsLoadingOutside(true);
        setIsFilteredData(true);
        const userId : number = user == null ? -1 : user.id;
        const fromTime : number = from == null ? 0 : from.valueOf() / 1000;
        const toTime : number = to == null ? Number.MAX_SAFE_INTEGER : to.valueOf() / 1000;
        if(hasAllLoaded.current) {
            let filtered: CommandSummary[];
            filtered = commands.filter((command) => command.timestamp >= (fromTime - 999) && command.timestamp <= (toTime + 999));
            if(userId != -1) {
                filtered = filtered.filter((command) => command.username == user?.username)
            }
            setFilteredCommands(filtered)
            setRowCount(filtered.length);
            setIsLoadingOutside(false);
        }
        else {
            deviceService.getPaginatedCommands(+deviceId,fromTime,
                toTime, page, 100,false, userId)
                .then((val) => {
                    setFilteredCommands(val.commands);

                    if(val.commands.length < 100) {
                        setRowCount(val.commands.length);
                        hasAllLoaded.current = false;
                    }
                    else
                        setRowCount(val.commands.length + 1);
                    setIsLoadingOutside(false);
                }).catch((error) => {
                setErrorMessage(error.response.data);
                setIsSuccess(false);
                setErrorPopupOpen(true);
                console.log(error)
            })
        }
    }
    useEffect(() => {
        if(!firstLoad.current) return;
        deviceService.getPaginatedCommands(+deviceId,0,
            Number.MAX_SAFE_INTEGER, 0, 100,true, -1)
            .then((val) => {
                setCommands(val.commands);
                setCurrentPageCommands(val.commands.slice(0,10))
                setUsernames(val.allUsers)
                setIsLoading(false);
                if(val.commands.length < 100) {
                    setRowCount(val.commands.length);
                    hasAllLoaded.current = true;
                }
                else
                    setRowCount(val.commands.length + 1);
            }).catch((error) => {
                console.log(error.response.data)
                setErrorMessage(error.response.data);
                setIsSuccess(false);
                setErrorPopupOpen(true);
        })
        firstLoad.current = false;
    }, []);

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

    const getNextPage = (model: GridPaginationModel) => {
        setIsLoading(true);
        if(commands.length <= model.page * model.pageSize && !hasAllLoaded) {
            setPage(page + 1);
            deviceService.getPaginatedCommands(+deviceId,0,
                Number.MAX_SAFE_INTEGER, page + 1, 100,false, -1)
                .then((val) => {
                    setCommands(prevState => [...prevState,...val.commands]);
                    setCurrentPageCommands(val.commands.slice(0,model.pageSize))
                    if(val.commands.length < 100)
                        setRowCount(rowCount + val.commands.length - 1);
                    else
                        setRowCount(rowCount + val.commands.length)
                    setIsLoading(false);
                }).catch((error) => {
                console.log(error.response.data)
                setErrorMessage(error.response.data);
                setIsLoading(false);
                setIsSuccess(false);
                setErrorPopupOpen(true);
            })
        }
        else {
            if(prevPageSize > model.pageSize)
                setCurrentPageCommands(currentPageCommands.slice(0,model.pageSize));
            else if(prevPageSize == model.pageSize)
                setCurrentPageCommands(commands.slice(model.page * prevPageSize, model.page * prevPageSize + model.pageSize));
            else {
                setCurrentPageCommands(commands.slice(prevPage * prevPageSize, prevPage * prevPageSize + model.pageSize));
            }
            setIsLoading(false);
            if(model.page != prevPage && model.pageSize != prevPageSize) return;
            else if(model.page != prevPage && model.pageSize == prevPageSize)
                setPrevPage(model.page);
            else if(model.page == prevPage && model.pageSize != prevPageSize)
                setPrevPageSize(model.pageSize);

        }
    }


    return (
        <>
            <CssBaseline/>
            <Grid container className={'dark-background'} height={'100%'}  justifyContent={'flex-start'}>
                <Grid item xs={0} sm={0} md={2} lg={2} xl={2}>
                    <SideNav userService={userService} isAdmin={sessionStorage.getItem("role") == RoleEnum.ROLE_ADMIN ||
                        sessionStorage.getItem("role") == RoleEnum.ROLE_SUPERADMIN} isSuperadmin={sessionStorage.getItem("role") == RoleEnum.ROLE_SUPERADMIN}/>
                </Grid>
                <Grid item height={'100%'}  xl={10} lg={10} md={10} sm={12} xs={12}
                      p={2}
                      className={'white-background'}
                      style={{borderRadius:'1.5em', overflowY:'scroll'}}
                      alignItems={'flex-start'}
                      ml={{xl: '20%', lg: '20%', md: '25%', sm: '0', xs: '0'}}
                      mt={{xl: 0, lg: 0, md: 0, sm: '64px', xs: '64px'}}>
                    <Grid item xs={12} sm={12} md={12} lg={12} xl={12} mb={4} pl={2} pr={2} alignItems={'center'} container>
                            <Typography variant={'h6'} mr={4}>Filter:</Typography>
                        <Autocomplete
                            disablePortal
                            id="user"
                            value={user}
                            onChange={(_event: any, newValue: UserIdUsernamePair | null) => {
                                setUser(newValue);
                            }}
                            inputValue={inputValueUser}
                            onInputChange={(_event, newInputValue) => {
                                setInputValueUser(newInputValue);
                            }}
                            options={usernames}
                            getOptionLabel={(val) => val.username}
                            sx={{ width: 250, marginRight:'5em' }}
                            renderInput={(params) => <TextField {...params} label="User" />}
                        />
                        <LocalizationProvider dateAdapter={AdapterDayjs}>
                            <MobileDateTimePicker
                                disableFuture={true}
                                sx={{marginLeft: '1em'}}
                                label="From"
                                value={from}
                                onChange={(newValue) => setFrom(newValue)}
                            />
                            <IconButton aria-label="resetFrom" onClick={resetFrom}>
                                <CloseIcon />
                            </IconButton>
                            <MobileDateTimePicker
                                sx={{marginLeft: '1em'}}
                                disableFuture={true}
                                label="To"
                                value={to}
                                onChange={(newValue) => setTo(newValue)}
                            />
                            <IconButton aria-label="resetTo" onClick={resetTo}>
                                <CloseIcon />
                            </IconButton>
                        </LocalizationProvider>
                        {!isLoadingOutside? null :
                            <CircularProgress sx={{position:'absolute',right:'150px'}} />
                        }
                        <Button variant={'contained'}
                                color={'secondary'}
                                onClick={filter}
                                sx={{marginLeft:'auto'}}
                                size={'large'}>Filter</Button>
                    </Grid>
                    {isFilteredData?
                        <DataGrid
                            sx={{borderRadius:'1.5em',width:'100%',
                                '&.MuiDataGrid-root--densityCompact .MuiDataGrid-cell': { py: '8px' },
                                '&.MuiDataGrid-root--densityStandard .MuiDataGrid-cell': { py: '15px' },
                                '&.MuiDataGrid-root--densityComfortable .MuiDataGrid-cell': { py: '22px' },}}
                            rows={filteredCommands}
                            columns={columns}
                            rowCount={rowCount}
                            disableColumnMenu={true}
                            getRowHeight={() => 'auto'}
                            columnHeaderHeight={70}
                            getRowId={(row) => row.timestamp}
                            initialState={{
                                pagination: {
                                    paginationModel: { page: 0, pageSize: 10 },
                                },
                            }}
                            pageSizeOptions={[10, 25, 50, 100]}
                        />
                        :
                    <DataGrid
                        sx={{borderRadius:'1.5em',width:'100%',
                            '&.MuiDataGrid-root--densityCompact .MuiDataGrid-cell': { py: '8px' },
                            '&.MuiDataGrid-root--densityStandard .MuiDataGrid-cell': { py: '15px' },
                            '&.MuiDataGrid-root--densityComfortable .MuiDataGrid-cell': { py: '22px' },}}
                        rows={currentPageCommands}
                        getRowHeight={() => 'auto'}
                        columnHeaderHeight={70}
                        columns={columns}
                        rowCount={rowCount}
                        loading={isLoading}
                        paginationMode="server"
                        onPaginationModelChange={getNextPage}
                        disableColumnMenu={true}
                        getRowId={(row) => row.timestamp}
                        initialState={{
                            pagination: {
                                paginationModel: { page: 0, pageSize: 10 },
                            },
                        }}
                        pageSizeOptions={[10,25,50,100]}
                    /> }
                </Grid>
            </Grid>
            <PopupMessage message={errorMessage} isSuccess={isSuccess} handleClose={handleErrorPopupClose}
                          open={errorPopupOpen}/>
        </>
    );
}