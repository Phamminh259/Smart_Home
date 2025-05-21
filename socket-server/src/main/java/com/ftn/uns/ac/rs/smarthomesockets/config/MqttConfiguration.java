package com.ftn.uns.ac.rs.smarthomesockets.config;

import lombok.Getter;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.*;

@Configuration
public class MqttConfiguration {
    private final Properties env;
    private final String broker;
    private final String uniqueClientIdentifier;
    @Getter
    private final MqttClient client;
    @Getter
    private final MqttMessageCallback mqttMessageCallback;

    public MqttConfiguration(Environment environment,
                             MqttMessageCallback mqttMessageCallback) throws Exception {
        this.mqttMessageCallback = mqttMessageCallback;
        this.env = new Properties();
        env.load(MqttConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));
        this.broker = String.format("tcp://%s:%s", environment.getProperty("mqtt.host"), environment.getProperty("mqtt.port"));
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
