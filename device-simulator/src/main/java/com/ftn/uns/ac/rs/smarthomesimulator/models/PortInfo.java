package com.ftn.uns.ac.rs.smarthomesimulator.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
//thông tin sạc tại cổng
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortInfo {
    private Date chargeStart;
    private double spentEnergy;
    private double carCapacity;
    private double carCharge;

    public PortInfo(int carCapacity, int carCharge) {
        this.chargeStart = new Date();
        this.spentEnergy = 0.0;
        this.carCapacity = carCapacity;
        this.carCharge = carCharge;
    }

    public boolean charge(double power, double chargeUntil) {
        spentEnergy += power;
        carCharge += power;
        double chargeUntilKWh = chargeUntil * carCapacity;
        if (carCharge >= chargeUntilKWh) {
            carCharge = chargeUntilKWh;
            return true;
        }
        return false;
    }
}
