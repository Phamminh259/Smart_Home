package com.ftn.uns.ac.rs.smarthomesockets.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftn.uns.ac.rs.smarthomesockets.models.ACValueDigest;
import com.ftn.uns.ac.rs.smarthomesockets.models.Measurement;
import com.ftn.uns.ac.rs.smarthomesockets.models.WMValueDigest;
import com.ftn.uns.ac.rs.smarthomesockets.models.dtos.SchedulesPerUser;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class MqttMessageCallback implements MqttCallback {
    private Logger log = org.slf4j.LoggerFactory.getLogger(MqttMessageCallback.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public MqttMessageCallback(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
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

    //TODO Menjaj mapu tagova ako ti treba nesto
    @Override public void messageArrived(String topic, MqttMessage mqttMessage) throws JsonProcessingException {
        String message = new String(mqttMessage.getPayload());
        if(topic.equals("ac")){
            try {
                ACValueDigest received = jsonMapper.readValue(message, ACValueDigest.class);
                messagingTemplate.convertAndSend("/ac/freshest/" + received.getDeviceId(), jsonMapper.writeValueAsString(received));
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        else if(topic.contains("status/ac")) {
            String[] data = message.split(",");
            messagingTemplate.convertAndSend("/ac/status/" + data[1],data[0]);
            //log.info("AC status changed to: " + data[0] + " for device: " + data[1]);
        }

        else if(topic.equals("wm")){
            try {
                WMValueDigest received = jsonMapper.readValue(message, WMValueDigest.class);
                messagingTemplate.convertAndSend("/wm/freshest/" + received.getDeviceId(), jsonMapper.writeValueAsString(received));
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        else if(topic.contains("status/wm")) {
            System.out.println(message);
            String[] data = message.split(",");
            messagingTemplate.convertAndSend("/wm/status/" + data[1],data[0]);
            //log.info("WM status changed to: " + data[0] + " for device: " + data[1]);
        }

        else if(topic.contains("status/sps")) {
            String[] data = message.split(",");
            messagingTemplate.convertAndSend("/sps/status/" + data[1],data[0]);
          //  log.info("SPS status changed to: " + data[0] + " for device: " + data[1]);
        }
        else if (topic.contains("status/battery")) {
            String[] data = message.split(",");
            messagingTemplate.convertAndSend("/battery/status/" + data[1],data[0]);
         //   log.info("Battery status changed to: " + data[0] + " for device: " + data[1]);
        }
        else if(topic.contains("scheduled")) {
            SchedulesPerUser schedules = jsonMapper.readValue(message, SchedulesPerUser.class);
            messagingTemplate.convertAndSend("/ac/schedules/" + schedules.getDeviceId(),schedules.getSchedules());
        } else {
            String[] data = message.split(",");
            String valueWithUnit = data[1];
            String deviceId = data[2];
            float value = Float.parseFloat(valueWithUnit.substring(0, valueWithUnit.length() - 1));
            Map<String, String> tags = new HashMap<>();
            tags.put("unit", valueWithUnit.substring(valueWithUnit.length() - 1));
            Measurement measurement = new Measurement(data[0], value, (new Date()).getTime(), tags);
            String toSend = jsonMapper.writeValueAsString(measurement);

            if (message.contains("temperature") || message.contains("humidity")) {
                messagingTemplate.convertAndSend("/thermometer/freshest/" + deviceId, toSend);
         //       log.info("Temperature changed to: " + toSend + " for device: " + deviceId);
            } else if (message.contains("totalConsumption")) {
                messagingTemplate.convertAndSend("/consumption/freshest/" + deviceId, toSend);
                log.info("Consumption changed to: " + toSend);
            } else if (message.contains("totalProduction")) {
                messagingTemplate.convertAndSend("/production/freshest/" + deviceId, toSend);
                log.info("Production changed to: " + toSend);
            }
        }
        //System.out.println("Message received. ID:" + mqttMessage.getId() + ", Message: " + message + ", Topic: " + topic);
    }

    @Override
    public void deliveryComplete(IMqttToken iMqttToken) {
       // System.out.println("Delivery complete, message ID: " + iMqttToken.getMessageId());
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
