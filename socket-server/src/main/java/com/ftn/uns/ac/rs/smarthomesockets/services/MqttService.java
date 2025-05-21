package com.ftn.uns.ac.rs.smarthomesockets.services;

import com.ftn.uns.ac.rs.smarthomesockets.config.MqttConfiguration;
import lombok.Getter;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.stereotype.Service;
// Đăng ký nhận các topic MQTT từ các thiết bị
// gửi và nhận dữ liệu MQTT từ ba
@Getter
@Service
public class MqttService {

    private final MqttConfiguration mqttConfiguration;

    public MqttService(MqttConfiguration mqttConfiguration) throws MqttException {
        this.mqttConfiguration = mqttConfiguration;
        this.mqttConfiguration.getClient().subscribe("measurements",2);
        this.mqttConfiguration.getClient().subscribe("consumed",2);
        this.mqttConfiguration.getClient().subscribe("power",2);
        this.mqttConfiguration.getClient().subscribe("ac",2);
        this.mqttConfiguration.getClient().subscribe("status/ac",2);
        this.mqttConfiguration.getClient().subscribe("wm",2);
        this.mqttConfiguration.getClient().subscribe("status/wm",2);
        this.mqttConfiguration.getClient().subscribe("scheduled",2);
        this.mqttConfiguration.getClient().subscribe("status/sps", 2);
        this.mqttConfiguration.getClient().subscribe("status/battery", 2);
    }

    public void publishMeasurementMessageLite(String message, String topic) throws MqttException {
        this.mqttConfiguration.getClient().publish(topic, new MqttMessage(message.getBytes()));
    }

}
