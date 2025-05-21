package com.ftn.uns.ac.rs.smarthomesimulator.threads;

import com.ftn.uns.ac.rs.smarthomesimulator.models.devices.Thermometer;
import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.PowerSource;
import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.TemperatureUnit;
import com.ftn.uns.ac.rs.smarthomesimulator.services.MqttService;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.slf4j.Logger;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
//hoạt động của nhiệt kế gửi dữ liệu qua MQTT.
public class ThermometerThread implements Runnable {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(ThermometerThread.class);
    private final Thermometer thermometer;
    private final TemperatureUnit unit;
    private final MqttService mqttService;
    private final Integer deviceId;
    private int count = 1;
    private final int INTERVAL = 5;
    private double powerConsumption = -1;
    private int humOrdinal = 1;
    private int tempOrdinal = 1;

    public ThermometerThread(Thermometer thermometer,
                             MqttService mqttService) {
        this.thermometer = thermometer;
        this.mqttService = mqttService;
        this.unit = thermometer.getTemperatureUnit();
        this.deviceId = thermometer.getId();
        this.powerConsumption = INTERVAL * 2.0 * thermometer.getEnergyConsumption() / (60 * 60);
    }

    @Override
    public void run() {
        try {
            generateValues();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            //System.err.println("Simulator thread interrupted");
        }
    }

    public void generateValues() throws InterruptedException {
        String[] seasons = {
                "WIN", "WIN", "SPR", "SPR", "SPR", "SUM",    // tháng 1-12
                "SUM", "SUM", "FAL", "FAL", "FAL", "WIN"
        };
// tg ban ngày
        int[][] dayStartEnd = new int[][]{
                {6, 17},   // Đông: 6h sáng -> 17h chiều
                {6, 18},   // Xuân: 6h sáng -> 18h chiều
                {5, 19},   // Hạ:   5h sáng -> 19h tối
                {6, 18}    // Thu:  6h sáng -> 18h chiều
        };

        // WIN, SPR, SUM, FAL {day, night}
        int[][][] typicalDayNightTemps = new int[][][]{
                {{15, 22}, {12, 18}},  // Đông: ngày (15–22°C), đêm (12–18°C)
                {{22, 28}, {18, 24}},  // Xuân
                {{28, 36}, {24, 30}},  // Hạ
                {{24, 30}, {20, 26}}   // Thu
        };

        // Độ ẩm ban ngày và ban đêm theo mùa
        // WIN, SPR, SUM, FAL {day, night}
        int[][] typicalDayNightHumidity = new int[][]{
                {70, 80},   // Đông: ban ngày 70%, ban đêm 80%
                {65, 75},   // Xuân
                {60, 70},   // Hạ
                {65, 75}    // Thu
        };

        double tempValue, humValue;
        while (true) {
            int[] currentDayStartEnd;
            int[][] currentTypicalDayNightTemps;
            int[] currentTypicalDayNightHumidity;
            int correctIndex;
            LocalDateTime now = LocalDateTime.now();   // mùa hiện tại
            String season = seasons[now.getMonthValue() - 1];
            // Lấy thông tin mùa và xác định mùa hiện tại
            correctIndex = switch (season) {
                case "WIN" -> 0;
                case "SPR" -> 1;
                case "SUM" -> 2;
                default -> 3;
            };
            currentDayStartEnd = dayStartEnd[correctIndex];
            currentTypicalDayNightTemps = typicalDayNightTemps[correctIndex];
            currentTypicalDayNightHumidity = typicalDayNightHumidity[correctIndex];
            if(now.getHour() >= currentDayStartEnd[0] && now.getHour() < currentDayStartEnd[1]) {
                int wholeSectionDuration = Math.abs(currentDayStartEnd[0] - currentDayStartEnd[1]);
                int multiplier = Math.abs(now.getHour() - currentDayStartEnd[0]);
                double hourAllowedTempDifference = (double) Math.abs(currentTypicalDayNightTemps[0][0] - currentTypicalDayNightTemps[0][1]) / (double) wholeSectionDuration;
                double hourAllowedHumDifference = (double) Math.abs(currentTypicalDayNightHumidity[0] - currentTypicalDayNightHumidity[1]) / (double) wholeSectionDuration;
                int halfDay = Math.abs(currentDayStartEnd[1] - currentDayStartEnd[0]) / 2;
                int multiplierHum = multiplier;
                if(now.getHour() >= halfDay) {
                    multiplier = multiplier % halfDay;
                }

                double maxTemp = hourAllowedTempDifference * multiplier + hourAllowedTempDifference;
                double minTemp = hourAllowedTempDifference * multiplier;
                double maxHum = hourAllowedHumDifference * multiplierHum + hourAllowedHumDifference;
                double minHum = hourAllowedHumDifference * multiplierHum;
                if(now.getHour() <= halfDay) {
                    tempValue = currentTypicalDayNightTemps[0][0] + (minTemp + Math.random() * (maxTemp - minTemp));
                }
                else {
                    tempValue = currentTypicalDayNightTemps[0][1] - (minTemp + Math.random() * (maxTemp - minTemp));
                }
                humValue = currentTypicalDayNightHumidity[1] - (minHum + Math.random() * (maxHum - minHum));
                sendAndDisplayMeasurements(tempValue, humValue);
            }
            else {
                int wholeSectionDuration = 24 - Math.abs(currentDayStartEnd[1] - currentDayStartEnd[0]);
                int divisor = 24 - currentDayStartEnd[1] + now.getHour();
                double hourAllowedTempDifference = (double) Math.abs(currentTypicalDayNightTemps[1][0] - currentTypicalDayNightTemps[1][1]) / (double) wholeSectionDuration;
                double hourAllowedHumDifference = (double) Math.abs(currentTypicalDayNightHumidity[0] - currentTypicalDayNightHumidity[1]) / (double) wholeSectionDuration;
                int halfNight = wholeSectionDuration / 2;
                int multiplier;
                boolean hasPassedHalf = false;
                if(divisor < 24) {
                    multiplier =  divisor;
                }
                else {
                    multiplier = divisor % 24;
                }
                int multiplierHum = multiplier;
                if(multiplier >= halfNight) {
                    hasPassedHalf = true;
                    multiplier = multiplier % halfNight;
                }
                double maxTemp = hourAllowedTempDifference * multiplier + hourAllowedTempDifference;
                double minTemp = hourAllowedTempDifference * multiplier;
                double maxHum = hourAllowedHumDifference * multiplierHum + hourAllowedHumDifference;
                double minHum = hourAllowedHumDifference * multiplierHum;
                if(!hasPassedHalf) {
                    tempValue = currentTypicalDayNightTemps[1][1] - (minTemp + Math.random() * (maxTemp - minTemp));
                }
                else {
                    tempValue = currentTypicalDayNightTemps[1][0] + (minTemp + Math.random() * (maxTemp - minTemp));
                }
                humValue = currentTypicalDayNightHumidity[0] + (minHum + Math.random() * (maxHum - minHum));
                sendAndDisplayMeasurements(tempValue, humValue);
            }

            Thread.sleep(INTERVAL * 1000);
        }
    }

    private void sendAndDisplayMeasurements(double temp, double humidity) {
        DecimalFormat df = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.ENGLISH));
        df.setRoundingMode(RoundingMode.CEILING);
        String msgHumidity = "humidity," + df.format(humidity) + "%," + deviceId;
        String msgTemp = "temperature," + df.format(temp) + "C," + deviceId;

        if (unit == TemperatureUnit.FAHRENHEIT) {
            temp =  temp * 1.8 + 32;
            msgTemp = "temperature," + df.format(temp) + "F," + deviceId;
        }

        //System.out.println(msgTemp + "\n" + msgHumidity);

        try {
            this.mqttService.publishMessageLite(msgTemp,"measurements");
            this.mqttService.publishMessageLite(msgHumidity,"measurements");

            if (count % 2 == 0) {
                count = 1;
                //System.out.println("Sending status message");
                this.mqttService.publishStatusMessageLite("status,1T," + deviceId);
                if (thermometer.getPowerSource().equals(PowerSource.HOUSE)) {
                    String message = "consumed," + powerConsumption + "p," + deviceId + "," + thermometer.getProperty().getId();
                    this.mqttService.publishPowerConsumptionMessage(message);
                    //logger.info("Sending power consumption: " + message);
                }
            } else {
                count++;
            }
        } catch (MqttException e) {
            //System.out.println("Error publishing message");
        }
    }

    public Thread getNewSimulatorThread() {
        Thread simulatorThread = new Thread(this);
        simulatorThread.start();
        return simulatorThread;
    }
}
