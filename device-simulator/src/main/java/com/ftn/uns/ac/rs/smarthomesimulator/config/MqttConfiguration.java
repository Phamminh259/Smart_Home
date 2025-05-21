package com.ftn.uns.ac.rs.smarthomesimulator.config;

import lombok.Getter;
import lombok.Setter;

import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import java.util.*;
//  kết nối MQTT client để giao tiếp với broker MQTT
public class MqttConfiguration {
    private final Properties env;
    private final String broker;
    private final String uniqueClientIdentifier;
    @Getter
    private final MqttClient client;
    @Getter
    @Setter
    private MqttCallback mqttMessageCallback;

    public MqttConfiguration(MqttCallback messageCallback) throws Exception {
        this.env = new Properties();
        this.mqttMessageCallback = messageCallback;
        env.load(MqttConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));
        this.broker = String.format("tcp://%s:%s", this.env.getProperty("mqtt.host"), this.env.getProperty("mqtt.port"));
        this.uniqueClientIdentifier = UUID.randomUUID().toString();
        this.client = this.mqttClient();
    }


    private MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(this.broker, this.uniqueClientIdentifier, new MemoryPersistence());
        client.setCallback(mqttMessageCallback);
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setCleanStart(false);
        options.setAutomaticReconnect(true);
        options.setUserName(this.env.getProperty("mqtt.username"));
        options.setPassword(Objects.requireNonNull(this.env.getProperty("mqtt.password")).getBytes());
        client.connect(options);
        return client;
    }
}
