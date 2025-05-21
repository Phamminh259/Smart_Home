package com.ftn.uns.ac.rs.smarthomesimulator.config;

import com.ftn.uns.ac.rs.smarthomesimulator.DeviceThreadManager;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.stereotype.Service;


// Ghi nhận trạng thái kết nối và phản hồi từ MQTT broker
@Service
public class MqttMessageCallback implements MqttCallback {

    public MqttMessageCallback() {}

    @Override
    /*
     * Callback in case the server disconnected from MQTT client.
     */
    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
        //System.out.println("Disconnected");
    }

    @Override
    public void mqttErrorOccurred(MqttException e) {
        //System.out.println("MQTT error occurred.");
    }

    @Override public void messageArrived(String topic, MqttMessage mqttMessage) {
        String message = new String(mqttMessage.getPayload());
    }

    @Override
    public void deliveryComplete(IMqttToken iMqttToken) {    
    }

    @Override
    public void connectComplete(boolean b, String s) {
        System.out.println("Connect complete, status:" + b + " " + s);
    }

    @Override
    public void authPacketArrived(int i, MqttProperties mqttProperties) {
        //System.out.println("Auth packet arrived , status:" +  i + " " + mqttProperties.getAuthenticationMethod());
    }
}
