package com.ftn.uns.ac.rs.smarthomesimulator.services;

import com.ftn.uns.ac.rs.smarthomesimulator.config.MqttConfiguration;
import com.ftn.uns.ac.rs.smarthomesimulator.config.MqttMessageCallback;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.stereotype.Service;
//publish thông tin từ thiết bị đến MQTT broker.
@Getter
@Setter
@Service
public class MqttService {
    private final MqttConfiguration mqttConfiguration;
    private final MqttMessageCallback mqttMessageCallback;
    private int tempOrdinal = 1;    // dem so lan gui
    private int humOrdinal = 1;

    public MqttService(MqttMessageCallback mqttMessageCallback) throws Exception {
        this.mqttMessageCallback = mqttMessageCallback;
        this.mqttConfiguration = new MqttConfiguration(mqttMessageCallback);
        //this.mqttConfiguration.getClient().subscribe("command/ac",2);
    }

    public void publishMessageLite(String message, String topic) throws MqttException {
        String[] split = message.split(",");
        this.mqttConfiguration.getClient().publish(topic, new MqttMessage(message.getBytes()));
        if(split[split.length - 1].equals("10001")) {   // message có ID là 10001 ghi log nhiệt độ và độ ẩm
            System.out.println(split[0]);
            if(split[0].equals("temperature")) {
                System.out.println("Sent TEMPERATURE (" + tempOrdinal + ") - " + new Date());
                tempOrdinal += 1;
            }
            else {
                System.out.println("Sent HUMIDITY (" + humOrdinal + ") - " + new Date());
                humOrdinal += 1;
            }
        }
    }

    public void publishStatusMessageLite(String message) throws MqttException {
        this.mqttConfiguration.getClient().publish("statuses", new MqttMessage(message.getBytes()));
    }

    public void publishPowerConsumptionMessage(String message) throws MqttException {
        this.mqttConfiguration.getClient().publish("consumed", new MqttMessage(message.getBytes()));
    }
}
