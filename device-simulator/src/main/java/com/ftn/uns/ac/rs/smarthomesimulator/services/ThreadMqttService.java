package com.ftn.uns.ac.rs.smarthomesimulator.services;

import com.ftn.uns.ac.rs.smarthomesimulator.config.MqttConfiguration;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;


public class ThreadMqttService {
    private final MqttConfiguration mqttConfiguration;

    public ThreadMqttService(MqttConfiguration mqttConfiguration) {
        this.mqttConfiguration = mqttConfiguration;
    }

    public void publishMessageLite(String message, String topic) throws MqttException {
        this.mqttConfiguration.getClient().publish(topic, new MqttMessage(message.getBytes()));
    }

    public void publishStatusMessageLite(String message) throws MqttException {
        this.mqttConfiguration.getClient().publish("statuses", new MqttMessage(message.getBytes()));
    }

    public void publishOnOff(String message, String device) throws MqttException {
        this.mqttConfiguration.getClient().publish("status/" + device, new MqttMessage(message.getBytes()));
    }
}
