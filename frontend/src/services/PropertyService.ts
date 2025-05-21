// @ts-nocheck
import axios from "axios";
import {Property} from "../models/Property";


export class PropertyService {
    private api_host = "http://localhost:8080"
    public getAllProperty():Promise<Property[]>{
        return axios({
            method: 'GET',
            url: `${this.api_host}/api/property/allProperties`,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }

    public getAllApprovedProperties():Promise<Property[]>{
        return axios({
            method: 'GET',
            url: `${this.api_host}/api/property/approvedProperties`,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }

    public getAllUnapprovedProperty():Promise<Property[]>{
        return axios({
            method: 'GET',
            url: `${this.api_host}/api/property/unapprovedProperties`,
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }
    public getProperty():Promise<Property[]>{
        return axios({
            method: 'GET',
            url: `${this.api_host}/api/property/getProperty/`+sessionStorage.getItem('username'),
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }

    public async getAllUserProperty(): Promise<Property[]> {
        return axios({
            method: 'GET',
            url: `${this.api_host}/api/property/approvedProperties/` + sessionStorage.getItem('username'),
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }

    public async registerProperty(property: Property): Promise<string> {
        return axios({
            method: 'POST',
            url: `${this.api_host}/api/property/registerProperty`,
            data: property,
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }
    public async approveProperty(propertyId: number): Promise<string> {
        return axios({
            method: 'PUT',
            url: `${this.api_host}/api/property/approve/` + propertyId.toString(),
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }
    public async denyProperty(propertyId: number): Promise<string> {
        return axios({
            method: 'PUT',
            url: `${this.api_host}/api/property/deny/` + propertyId.toString(),
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': 'Bearer ' + sessionStorage.getItem('user')
            },
        }).then((response) => response.data
        ).catch((err) => {
            throw err
        });
    }
}