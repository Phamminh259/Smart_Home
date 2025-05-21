package com.ftn.uns.ac.rs.smarthomesockets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftn.uns.ac.rs.smarthomesockets.models.ACCommand;
import com.ftn.uns.ac.rs.smarthomesockets.models.WMCommand;
import com.ftn.uns.ac.rs.smarthomesockets.models.dtos.DeviceCapabilities;
import com.ftn.uns.ac.rs.smarthomesockets.services.DeviceService;
import com.ftn.uns.ac.rs.smarthomesockets.services.MqttService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

// điều khiển và phản hồi STOMP WebSocket
@Controller
public class MessageController {

    private final ObjectMapper mapper = new ObjectMapper();
    private final SimpMessagingTemplate messagingTemplate;
    private final DeviceService deviceService;
    private final MqttService mqttService;

    public MessageController(SimpMessagingTemplate messagingTemplate,
                             DeviceService deviceService,
                             MqttService mqttService) {
        this.messagingTemplate = messagingTemplate;
        this.deviceService = deviceService;
        this.mqttService = mqttService;
    }

    @MessageMapping("/command/ac")
    public void sendCommandMessage(String message){
        try{
            ACCommand receivedCommand = mapper.readValue(message, ACCommand.class);
            mqttService.publishMeasurementMessageLite(message,"command/ac/" + receivedCommand.getDeviceId());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @MessageMapping("/command/wm")
    public void sendCommandMessageWm(String message){
        try{
            WMCommand receivedCommand = mapper.readValue(message, WMCommand.class);
            mqttService.publishMeasurementMessageLite(message,"command/wm/" + receivedCommand.getDeviceId());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @MessageMapping("/capabilities/ac")
    public void getDeviceCapabilitiesAc(String deviceId){
        try{
            int deviceIdInt = Integer.parseInt(deviceId);
            DeviceCapabilities capabilities = deviceService.getDeviceCapabilities(deviceIdInt);
            messagingTemplate.convertAndSend("/ac/capabilities/" + deviceIdInt, mapper.writeValueAsString(capabilities));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @MessageMapping("/capabilities/wm")
    public void getDeviceCapabilitiesWM(String deviceId){
        try{
            int deviceIdInt = Integer.parseInt(deviceId);
            DeviceCapabilities capabilities = deviceService.getDeviceCapabilities(deviceIdInt);
            messagingTemplate.convertAndSend("/wm/capabilities/" + deviceIdInt, mapper.writeValueAsString(capabilities));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
