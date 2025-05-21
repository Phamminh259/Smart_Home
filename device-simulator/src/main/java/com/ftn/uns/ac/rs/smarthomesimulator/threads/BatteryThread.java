package com.ftn.uns.ac.rs.smarthomesimulator.threads;

import com.ftn.uns.ac.rs.smarthomesimulator.config.MqttConfiguration;
import com.ftn.uns.ac.rs.smarthomesimulator.models.devices.Battery;
import com.ftn.uns.ac.rs.smarthomesimulator.repositories.BatteryRepository;
import com.ftn.uns.ac.rs.smarthomesimulator.services.ThreadMqttService;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
//  trạng thái.,dung lượng,trạng thái tổng thể qua mqtt
public class BatteryThread implements Runnable {
    public static final int INTERVAL = 30;
    private static final Logger logger = LoggerFactory.getLogger(BatteryThread.class);
    private final Integer deviceId;
    private Battery battery;
    private MqttConfiguration mqttConfiguration;
    private ThreadMqttService mqttService;
    private final BatteryRepository batteryRepository;

    private class MqttBatteryMessageCallback implements MqttCallback {
        @Override
        public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {}

        @Override
        public void mqttErrorOccurred(MqttException e) {System.err.println(e.getMessage());}

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) {
            try {
                String message = new String(mqttMessage.getPayload());
            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        @Override
        public void deliveryComplete(IMqttToken iMqttToken) {}

        @Override
        public void connectComplete(boolean b, String s) {}

        @Override
        public void authPacketArrived(int i, MqttProperties mqttProperties) {}
    }

    public BatteryThread(Battery battery,
                         BatteryRepository batteryRepository) {
        try {
            this.mqttConfiguration = new MqttConfiguration(new MqttBatteryMessageCallback());
            this.mqttService = new ThreadMqttService(this.mqttConfiguration);
        } catch (Exception e) {
            logger.error("Error while creating mqtt configuration: " + e.getMessage());
        }
        this.battery = battery;
        this.deviceId = battery.getId();
        this.batteryRepository = batteryRepository;
    }

    @Override
    public void run() {
        try {
            generateValues();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted: " + e.getMessage());
        }
    }

    public void generateValues() throws InterruptedException {
        while (true) {
            sendInternalState(); // trang thái
            sendOccupation(); //dl hiện tại
            Thread.sleep(INTERVAL * 1000);// nghỉ 30s
        }
    }

    public void sendInternalState() {
        try {
            String status = "ON";
            String message = status + "," + deviceId;
            mqttService.publishOnOff(message, "battery");
            logger.info("Sending message: " + message);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void sendOccupation() {
        this.battery = batteryRepository.findById(deviceId).orElse(battery);
        DecimalFormat df = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.ENGLISH));
        df.setRoundingMode(RoundingMode.CEILING);
        String msg = "battery," + df.format(battery.getOccupiedCapacity()) + "p," + deviceId;

        logger.info("Sending message: " + msg);

        try {
            this.mqttService.publishMessageLite(msg,"measurements");
            logger.info("Sending status message");
            this.mqttService.publishStatusMessageLite("status,1T," + deviceId);
        } catch (MqttException e) {
            logger.error("Error while sending message: " + msg);
        }
    }

    public Thread getNewSimulatorThread() {
        Thread simulatorThread = new Thread(this);
        simulatorThread.start();
        return simulatorThread;
    }
}
