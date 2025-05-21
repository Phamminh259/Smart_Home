package com.ftn.uns.ac.rs.smarthome.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftn.uns.ac.rs.smarthome.PowerManager;
import com.ftn.uns.ac.rs.smarthome.StillThereDevicesManager;
import com.ftn.uns.ac.rs.smarthome.models.ACStateChange;
import com.ftn.uns.ac.rs.smarthome.services.InfluxService;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IDeviceService;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//Xử lý tin nhắn MQTT đến
@Service
public class MqttMessageCallback implements MqttCallback {
    private Logger log = org.slf4j.LoggerFactory.getLogger(MqttMessageCallback.class);
    private final InfluxService influxService;
    private final IDeviceService deviceService;
    private final StillThereDevicesManager stillThereDevicesManager;
    private final PowerManager powerManager;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public MqttMessageCallback(InfluxService influxService,
                               IDeviceService deviceService,
                               StillThereDevicesManager stillThereDevicesManager,
                               PowerManager powerManager) {
        this.influxService = influxService;
        this.deviceService = deviceService;
        this.stillThereDevicesManager = stillThereDevicesManager;
        this.powerManager = powerManager;
    }

    @Override
    /*
     * Callback in case the server disconnected from MQTT client.
     */
    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
        System.out.println("Disconnected");
    }

    /*
     * Callback in case an error occurred regarding MQTT client.
     */
    @Override
    public void mqttErrorOccurred(MqttException e) {
        System.out.println("MQTT error occurred.");
    }

    @Override public void messageArrived(String topic, MqttMessage mqttMessage) {
        String message = new String(mqttMessage.getPayload());
        log.info("Message arrived: " + message + ", ID: " + mqttMessage.getId());
        if (topic.contains("states")) {
            try {
                Map<String,String> map = new HashMap<>();
                ACStateChange stateChange = jsonMapper.readValue(message, ACStateChange.class);
                if(stateChange.getExtraInfo() != null)
                    map = stateChange.getExtraInfo();
                map.put("userId",stateChange.getUserId().toString());
                map.put("deviceId", stateChange.getDeviceId().toString());
                influxService.save("states", stateChange.getChange(), new Date(), map);
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        } else if (topic.contains("charger")) {
            String[] data = message.split(",");
            String state = data[0];
            String portNum = data[1];
            String deviceId = data[4];

            Map<String,String> map = new HashMap<>();
            map.put("portNum", portNum);
            map.put("userId", "0");
            map.put("deviceId", deviceId);
            if (state.equals("START")) {
                String carCapacity = data[2];
                String carCharge = data[3];

                map.put("carCapacity", carCapacity);
                map.put("carCharge", carCharge);
                influxService.save("states", "START_CHARGE", new Date(), map);
            } else if (state.equals("END")) {
                String carCharge = data[2];
                String spentEnergy = data[3];

                map.put("carCharge", carCharge);
                map.put("spentEnergy", spentEnergy);
                influxService.save("states", "END_CHARGE", new Date(), map);
            }
        } else {
            String[] data = message.split(",");
            String measurementObject = data[0];
            String valueWithUnit = data[1];
            float value = Float.parseFloat(valueWithUnit.substring(0, valueWithUnit.length() - 1));
            char unit = valueWithUnit.charAt(valueWithUnit.length() - 1);
            String deviceIdStr = data[2];

            int deviceId = Integer.parseInt(deviceIdStr);
            if (measurementObject.equals("status") && value >= 1 &&
                    stillThereDevicesManager.isntThere(deviceId)) {
                deviceService.setDeviceStillThere(deviceId);
                stillThereDevicesManager.add(deviceId);
            }

            if (measurementObject.equals("produced")) {
                String propertyIdStr = data[3];
                int propertyId = Integer.parseInt(propertyIdStr);
                log.info("Adding production for property " + propertyId + " with value " + value);
                powerManager.addProduction(propertyId, value);
                influxService.save(measurementObject, value, new Date(),
                        Map.of("deviceId", deviceIdStr, "unit", String.valueOf(unit), "property", propertyIdStr));
            } else if (measurementObject.equals("consumed")) {
                String propertyIdStr = data[3];
                int propertyId = Integer.parseInt(propertyIdStr);
                log.info("Adding consumption for property " + propertyId + " with value " + value);
                powerManager.addConsumption(propertyId, value);
                influxService.save(measurementObject, value, new Date(),
                        Map.of("deviceId", deviceIdStr, "unit", String.valueOf(unit), "property", propertyIdStr));
            } else {
                influxService.save(measurementObject, value, new Date(),
                        Map.of("deviceId", deviceIdStr, "unit", String.valueOf(unit)));
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttToken iMqttToken) {
        System.out.println("Delivery complete, message ID: " + iMqttToken.getMessageId());
    }

    @Override
    public void connectComplete(boolean b, String s) {

        System.out.println("Connect complete, status:" + b + " " + s);
    }

    @Override
    public void authPacketArrived(int i, MqttProperties mqttProperties) {
        System.out.println("Auth packet arrived , status:" +  i + " " + mqttProperties.getAuthenticationMethod());
    }
}
