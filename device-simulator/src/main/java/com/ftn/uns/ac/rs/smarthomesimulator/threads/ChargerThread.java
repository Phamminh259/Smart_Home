package com.ftn.uns.ac.rs.smarthomesimulator.threads;

import com.ftn.uns.ac.rs.smarthomesimulator.config.MqttConfiguration;
import com.ftn.uns.ac.rs.smarthomesimulator.models.PortInfo;
import com.ftn.uns.ac.rs.smarthomesimulator.models.devices.Charger;
import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.PowerSource;
import com.ftn.uns.ac.rs.smarthomesimulator.services.ThreadMqttService;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
//Mô phỏng quá trình sạc xe,gửi thông tin tiêu thụ điện năng và trạng thái qua MQTT.
public class ChargerThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ChargerThread.class);
    public static final int INTERVAL = 30;
    public static final int PROBABILITY = 500;
    private final Charger charger;
    private final PortInfo[] ports;
    private int portsInUse = 0;
    private MqttConfiguration mqttConfiguration;
    private ThreadMqttService mqttService;
    private final double powerConsumption;

    private class MqttChargerMessageCallback implements MqttCallback {
        @Override
        public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {}

        @Override
        public void mqttErrorOccurred(MqttException e) {System.err.println(e.getMessage());}

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) {
            try {
                String message = new String(mqttMessage.getPayload());
                logger.info("Message received: " + message);
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
// thụ điện từ Wh mỗi giờ
    public ChargerThread(Charger charger) {
        this.charger = charger;
        this.ports = new PortInfo[charger.getNumberOfPorts()];
        this.powerConsumption = INTERVAL * charger.getEnergyConsumption() / (60 * 60);

        try {
            this.mqttConfiguration = new MqttConfiguration(new MqttChargerMessageCallback());
            this.mqttService = new ThreadMqttService(this.mqttConfiguration);
        } catch (Exception e) {
            logger.error("Error while creating mqtt configuration: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (this) {
                    runChecks();
                }
                Thread.sleep(INTERVAL * 1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted: " + e.getMessage());
        }
    }

    public void runChecks() throws InterruptedException {
        sendInternalState();
        if (charger.getPowerSource().equals(PowerSource.HOUSE) && portsInUse > 0) {
            sendPowerConsumption();
        }
        handleCurrentlyChargingCars();
        handleNewCars();
    }

    private void handleNewCars() {
        boolean newCarCame = ThreadLocalRandom.current().nextInt(0, PROBABILITY) == 0;
        if (!newCarCame || portsInUse == charger.getNumberOfPorts()) {
            return;
        }
        int carCapacity = ThreadLocalRandom.current().nextInt(20, 100);
        int carCharge = ThreadLocalRandom.current().nextInt(0, (int) Math.floor(carCapacity * charger.getChargeUntil()));
        PortInfo port = new PortInfo(carCapacity, carCharge);

        int portNum = -1;
        for (int i = 0; i < charger.getNumberOfPorts(); i++) {
            if (ports[i] == null) {
                ports[i] = port;
                portsInUse++;
                portNum = i + 1;
                break;
            }
        }

        sendStartMsg(port, portNum);
        logger.info("New car came: " + port);
    }

    private void sendStartMsg(PortInfo port, int portNum) {
        String msg = "START," + portNum + "," + port.getCarCapacity() + "," + port.getCarCharge() + "," + charger.getId();// xe mới sạc
        try {
            this.mqttConfiguration.getClient().publish("charger", new MqttMessage(msg.getBytes()));
            logger.info("Message sent: " + msg);
        } catch (Exception e) {
            logger.error("Error while sending message: " + msg);
            e.printStackTrace();
        }
    }

    private void sendEndMsg(PortInfo port, int portNum) {
        String msg = "END," + portNum + "," + port.getCarCharge() + "," + port.getSpentEnergy() + "," + charger.getId();// rời sạc
        try {
            this.mqttConfiguration.getClient().publish("charger", new MqttMessage(msg.getBytes()));
            logger.info("Message sent: " + msg);
        } catch (Exception e) {
            logger.error("Error while sending message: " + msg);
            e.printStackTrace();
        }
    }

    private void handleCurrentlyChargingCars() {
        double kWUsed = ((charger.getPower() / (60.0 * 60)) * INTERVAL) / portsInUse;
        // Lượng điện dùng cho mỗi cổng: công suất chia cho giây, nhân 30 giây và chia đều cho các cổng đang hoạt động.

        for (int i = 0; i < charger.getNumberOfPorts(); i++) {
            PortInfo port = ports[i];
            if (port == null) {
                continue;
            }
            double chargeUntil = port.getCarCapacity() * charger.getChargeUntil();
            boolean full = port.charge(kWUsed, chargeUntil);
            boolean decidedToLeave = ThreadLocalRandom.current().nextInt(0, PROBABILITY) == 0;

            logger.info("Car " + (i + 1) + " charging: " + port + " device: " + charger.getId());
            if (full || decidedToLeave) {
                logger.info("Car " + (i + 1) + " left: " + port + " device: " + charger.getId());
                sendEndMsg(port, i + 1);
                ports[i] = null;
                portsInUse--;
            }
        }
    }

    private void sendPowerConsumption() {
        String message = "consumed," + powerConsumption + "p," + charger.getId() + "," + charger.getProperty().getId();// tiêu thụ điện
        logger.info("Sending power consumption: " + message);
        try {
            this.mqttConfiguration.getClient().publish("consumed", new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
            logger.error("Error while sending power consumption: " + e.getMessage());
        }
    }

    public void sendInternalState() {
        try {
            String status = "ON";
            String message = status + "," + charger.getId();
            mqttService.publishOnOff(message, "charger");
            logger.info("Sending message: " + message);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public Thread getNewSimulatorThread() {
        Thread simulatorThread = new Thread(this);
        simulatorThread.start();
        return simulatorThread;
    }
}
