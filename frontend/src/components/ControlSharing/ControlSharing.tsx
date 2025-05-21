// @ts-nocheck
import React, {useEffect} from "react";
import {
    Autocomplete, Button,
    Dialog,
    DialogContent,
    DialogTitle,
    Grid, IconButton, TextField
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import {DataGrid, GridColDef, GridRenderCellParams} from "@mui/x-data-grid";
import {PopupMessage} from "../PopupMessage/PopupMessage.tsx";
import {UserSearchInfo} from "../../models/UserSearchInfo.ts";
import {UserService} from "../../services/UserService.ts";
import {DeviceService} from "../../services/DeviceService.ts";
import {DeviceControlDetails} from "../../models/DeviceControlDetails.ts";
import {DeviceControlDto} from "../../models/DeviceControlDto.ts";

interface ControlSharingProps {
    open: boolean,
    handleClose: () => void,
    userService: UserService,
    deviceService: DeviceService,
    deviceOrPropertyId: number,
    name: string,
    isDevice: boolean
}


export function ControlSharing({open, handleClose, userService, deviceService, deviceOrPropertyId, name, isDevice}: ControlSharingProps) {

    const [errorMessage, setErrorMessage] = React.useState<string>("");
    const [errorPopupOpen, setErrorPopupOpen] = React.useState<boolean>(false);
    const [isSuccess, setIsSuccess] = React.useState(true);
    const [inputValueUser, setInputValueUser] = React.useState('');
    const [usernames, setUsernames] = React.useState<UserSearchInfo[]>([]);
    const [selectedUsers, setSelectedUsers] = React.useState<UserSearchInfo[]>([]);
    const [lastVal, setLastVal] = React.useState<string>('');
    const [fillPrev, setFillPrev] = React.useState<boolean>(false);
    const [selectedUser, setSelectedUser] = React.useState<UserSearchInfo | null>(null);
    const [userShares, setUserShares] = React.useState<DeviceControlDetails[]>([]);
    const [originalShares, setOriginalShares] = React.useState<DeviceControlDetails[]>([]);
    const removeSelected = (id: number) => {
        setSelectedUsers(selectedUsers.filter(item => item.id !== id));
        if(!originalShares.some(original => original.userId == id))
            setUserShares(userShares.filter(share => share.userId != id));
        else
            setUserShares(userShares.map(obj => obj.userId === id ? { ...obj, action: "d" } : obj ));

    }
    const columns: GridColDef[] = [
        { field: 'username',
            headerName: 'User',
            type:'string',
            minWidth: 220,
            headerClassName: 'bold-and-colored',
            cellClassName: 'bold-and-colored',
            align:'center',
            headerAlign:'center',
        },
        { field: 'fullName',
            headerName: 'Full name',
            type:'string',
            minWidth: 220,
            align:'center',
            headerAlign:'center',
        },
        { field:'id',
            headerName: '',
            headerClassName:'gray',
            cellClassName: 'gray',
            sortable: false,
            align:'center',
            renderCell: (params: GridRenderCellParams<any,number>) => (
                <CloseIcon color={'error'} onClick={() => removeSelected(params.value!)}/>
            )
        }
    ];

    const addUser = (newUser: UserSearchInfo | null) => {
        if(newUser == null) return;
        if(selectedUsers.some(user => user.id === newUser?.id!)) return;
        setSelectedUsers([...selectedUsers, newUser!]);
        const add : DeviceControlDetails = {
            userId: newUser!.id,
            action: "a"
        }
        setUserShares([...userShares, add]);
    }
    const handleErrorPopupClose = (reason?: string) => {
        if (reason === 'clickaway') return;
        setErrorPopupOpen(false);
    };

    const close = () => { handleClose(); }

    const share = () => {
        if(userShares.length == 0 && originalShares.length == 0) {
            setErrorMessage("Please add users to share control!");
            setIsSuccess(false);
            setErrorPopupOpen(true);
            return;
        }
        const dto : DeviceControlDto = {
            details: userShares
        }
        if(isDevice) {
            deviceService.editDeviceSharedControl(deviceOrPropertyId, dto).then(() => {
                setErrorMessage(`Control over ${name} successfully edited!`);
                setIsSuccess(true);
                setErrorPopupOpen(true);
            }).catch((err) => {
                setErrorMessage(err.response.data);
                setIsSuccess(false);
                setErrorPopupOpen(true);
            })
        }
        else {
            deviceService.editPropertySharedControl(deviceOrPropertyId, dto).then(() => {
                setErrorMessage(`Control over ${name} successfully edited!`);
                setIsSuccess(true);
                setErrorPopupOpen(true);
            }).catch((err) => {
                setErrorMessage(err.response.data);
                setIsSuccess(false);
                setErrorPopupOpen(true);
            })
        }
    }

    useEffect(() => {
        if(deviceOrPropertyId == -1 || deviceOrPropertyId == undefined) return;
        if(isDevice) {
            deviceService.getDeviceSharedControl(deviceOrPropertyId).then((shares) => {
                setSelectedUsers(shares);
                shares.forEach(share => {
                    const detail: DeviceControlDetails = {
                        userId: share.id,
                        action: "n"
                    }
                    userShares.push(detail);
                    originalShares.push({...detail});
                })
                setUserShares(userShares);
                setOriginalShares(originalShares);
            }).catch((err) => {
                setErrorMessage(err.response?.data);
                setIsSuccess(false);
                setErrorPopupOpen(true);
            })
        }
        else {
            deviceService.getPropertySharedControl(deviceOrPropertyId).then((shares) => {
                setSelectedUsers(shares);
                shares.forEach(share => {
                    const detail: DeviceControlDetails = {
                        userId: share.id,
                        action: "n"
                    }
                    userShares.push(detail);
                    originalShares.push({...detail});
                })
                setUserShares(userShares);
                setOriginalShares(originalShares);
            }).catch((err) => {
                setErrorMessage(err.response?.data);
                setIsSuccess(false);
                setErrorPopupOpen(true);
            })
        }
    },[deviceOrPropertyId] );


    const findByKey = () => {
        userService.getUsersByKey(lastVal).then((users) => {
            if(usernames.some(item2 =>
                users.some(item1 => item1.id == item2.id))) return;
            setUsernames([...usernames, ...users]);
            setInputValueUser(lastVal);
            setFillPrev(true);
        }).catch((err) => {
            setErrorMessage(err.response.data);
            setIsSuccess(false);
            setErrorPopupOpen(true);
        })
    }

    return (
        <>
            <React.Fragment>
                <Dialog
                    maxWidth={'sm'}
                    fullWidth={true}
                    open={open}
                    onClose={handleClose}>
                    <DialogTitle textAlign={'center'}>Control Sharing For {name}</DialogTitle>
                    <DialogContent>
                        <Grid container rowSpacing={3} alignItems={'center'}>
                            <Grid item container xs={12} sm={12} md={12} lg={9} xl={9}>
                                <Autocomplete
                                    disablePortal
                                    id="user"
                                    sx={{marginTop:2}}
                                    fullWidth={true}
                                    onChange={(_event: any, newValue: UserSearchInfo | null) => {
                                        setSelectedUser(newValue);
                                        addUser(newValue);
                                    }}
                                    inputValue={inputValueUser}
                                    value={selectedUser}
                                    onSelect={() => setFillPrev(false)}
                                    onInputChange={(_event, newInputValue) => {
                                        if(fillPrev)
                                            setInputValueUser(lastVal)
                                        else
                                            setInputValueUser(newInputValue);
                                        if (newInputValue != '' && newInputValue != null) {
                                            setLastVal(newInputValue);
                                        }
                                    }}
                                    options={usernames}
                                    getOptionLabel={(val) => val.username + " (" + val.fullName + ")"}
                                    renderInput={(params) => <TextField {...params} label="Search user by username, name or surname" />}
                                />
                            </Grid>
                            <Grid container item xs={12} sm={12} md={12} lg={3} xl={3} justifyContent={'center'} alignItems={'center'}>
                                <Button color={'primary'}
                                        sx={{marginTop:2}}
                                        type={'button'}
                                        variant={'contained'}
                                        onClick={findByKey}>Filter</Button>
                            </Grid>
                            <Grid item container xs={12} sm={12} md={12} lg={12} xl={12}>
                                <DataGrid
                                    sx={{borderRadius:'1.5em',width:'100%', height:'500px',
                                        '&.MuiDataGrid-root--densityCompact .MuiDataGrid-cell': { py: '10px' },
                                        '&.MuiDataGrid-root--densityStandard .MuiDataGrid-cell': { py: '10px' },
                                        '&.MuiDataGrid-root--densityComfortable .MuiDataGrid-cell': { py: '10px' },}}
                                    rows={selectedUsers}
                                    columns={columns}
                                    disableColumnMenu={true}
                                    getRowHeight={() => 'auto'}
                                    columnHeaderHeight={50}
                                    getRowId={(row) => row.id}
                                    disableRowSelectionOnClick
                                    initialState={{
                                        pagination: {
                                            paginationModel: { page: 0, pageSize: 10 },
                                        },
                                    }}
                                    pageSizeOptions={[10, 25, 50, 100]}
                                />
                            </Grid>
                            <Grid item container justifyContent={'center'} xs={12} sm={12} md={12} lg={12} xl={12}>
                                <Button color={'primary'}
                                        variant={'contained'}
                                        onClick={share}>Edit Shared Control</Button>
                            </Grid>
                         </Grid>
                        <IconButton aria-label="close" sx={{position:'absolute',top:'2px',right:'3px'}} onClick={close}>
                            <CloseIcon />
                        </IconButton>
                    </DialogContent>

                </Dialog>
                <PopupMessage message={errorMessage} isSuccess={isSuccess} handleClose={handleErrorPopupClose}
                              open={errorPopupOpen}/>
            </React.Fragment>
        </>
    );
}