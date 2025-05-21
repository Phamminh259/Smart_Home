package com.ftn.uns.ac.rs.smarthomesimulator.threads;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.ftn.uns.ac.rs.smarthomesimulator.config.MqttConfiguration;
import com.ftn.uns.ac.rs.smarthomesimulator.models.*;
import com.ftn.uns.ac.rs.smarthomesimulator.models.devices.WashingMachine;
import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.*;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
// hoạt động của thiết bị máy giặt
public class WashingMachineThread implements Runnable {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(ACThread.class);
    private final WashingMachine wm;
    private final ObjectMapper mapper = new ObjectMapper();
    private WMCommand settings;
    private boolean isOff = true;
    private MqttConfiguration mqttConfiguration;
    private final int id = 1001;
    private String sent = "";

    private final Map<WMMode, Integer> modeInMinutesDuration = new HashMap<>() {{
       put(WMMode.COTTONS, 90); // thời gian giặt các loại vải khác nhau
       put(WMMode.SYNTHETICS, 75);
       put(WMMode.DAILY_EXPRESS, 30);
       put(WMMode.WOOL, 100);
       put(WMMode.DARK_WASH, 60);
       put(WMMode.OUTDOOR, 60);
       put(WMMode.SHIRTS, 90);
       put(WMMode.DUVET, 45);
       put(WMMode.MIXED, 90);
       put(WMMode.STEAM, 25);
       put(WMMode.RINSE_SPIN, 25);
       put(WMMode.SPIN_ONLY, 10);
       put(WMMode.HYGIENE, 75);
    }};
    private final Map<Long, ScheduledFuture<?>> scheduledThread = new ConcurrentHashMap<>();
    private final Map<Long, Scheduled> scheduledDetails = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(5);
    private final AtomicLong scheduledThreadCount = new AtomicLong(0);
    private final int INTERVAL = 5;
    private double powerConsumption = -1;

    private int onOffOrdinal = 1;
    private int scheduleOrdinal = 1;
    private int stateOrdinal = 1;
// nghe tin nhắn
    private class MqttWMMessageCallback implements MqttCallback {

        @Override
        public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {}

        @Override
        public void mqttErrorOccurred(MqttException e) {System.err.println(e.getMessage());}

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                String message = new String(mqttMessage.getPayload());
                WMCommand receivedCommand = mapper.readValue(message, WMCommand.class);
                CommandType commandType = receivedCommand.getCommandType();
                if(commandType == CommandType.ON) {
                    if(wm.getId() == id) {
                        System.out.println("Received ON (" + onOffOrdinal + ") - " + new Date());
                        onOffOrdinal += 1;
                    }
                    publishOn(receivedCommand);
                    sent = "ON";
                    isOff = false;
                    settings = receivedCommand;
                    scheduler.schedule( () -> {
                                isOff = true;
                                publishStateMessage(new ACStateChange(0,
                                        wm.getId(),
                                        WMState.OFF.toString(),null));
                    },
                    modeInMinutesDuration.get(receivedCommand.getCommandParams().getMode()) * 1000,
                    TimeUnit.MILLISECONDS);
                }

                else if(commandType == CommandType.CANCEL_SCHEDULED) {
                    if(wm.getId() == id) {
                        System.out.println("Received CANCEL (" + scheduleOrdinal + ") - " + new Date());
                        scheduleOrdinal += 1;
                    }
                    sent = "CANCEL";
                    removeScheduledThread(receivedCommand);
                    getSchedules();
                }
                else if(commandType == CommandType.GET_SCHEDULES) {
                    if(wm.getId() == id) {
                        System.out.println("Received GET SCHEDULES (" + scheduleOrdinal + ") - " + new Date());
                        scheduleOrdinal += 1;
                    }
                    sent = "GET SCHEDULES";
                    getSchedules();
                }
                else {
                    if(wm.getId() == id) {
                        System.out.println("Received SCHEDULE (" + scheduleOrdinal + ") - " + new Date());
                        scheduleOrdinal += 1;
                    }
                    sent = "SCHEDULE";
                    scheduleThread(receivedCommand);
                    getSchedules();
                }
            }
            catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        @Override
        public void deliveryComplete(IMqttToken iMqttToken) {
           if(wm.getId() == id) {
                System.out.println("SENT " + new Date());
           }
        }

        @Override
        public void connectComplete(boolean b, String s) {}

        @Override
        public void authPacketArrived(int i, MqttProperties mqttProperties) {}
    }
    public WashingMachineThread(WashingMachine wm,
                    WMCommand settings) {
        try {
            this.mqttConfiguration = new MqttConfiguration(new WashingMachineThread.MqttWMMessageCallback());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        this.wm = wm;
        this.settings = settings;
        this.powerConsumption = INTERVAL * 2.0 * wm.getEnergyConsumption() / (60 * 60);
    }

    @Override
    public void run() {
        try {
            mqttConfiguration.getClient().subscribe("command/wm/" + wm.getId(), 0);
            loop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Simulator thread interrupted");
        }
        catch(MqttException ex) {
            //System.err.println("MQTT Error: " + ex.getMessage());
        }
    }

    public void loop() throws InterruptedException {
        int count = 1;
        while(true) {
            sendInternalState();
            if (!isOff) {
                if (wm.getPowerSource().equals(PowerSource.HOUSE)) {
                    if (count % 2 == 0) {
                        sendPowerConsumption();
                    }
                    count++;
                }
                sendStateAndStatus(generateValue());
            }

            Thread.sleep(INTERVAL * 1000);
        }
    }

    private void sendPowerConsumption() {
        String message = "consumed," + powerConsumption + "p," + wm.getId() + "," + wm.getProperty().getId();
        //log.info("Sending power consumption: " + message);
        try {
            this.mqttConfiguration.getClient().publish("consumed", new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
            //log.error("Error while sending power consumption: " + e.getMessage());
        }
    }

    private WMValueDigest generateValue() {
        return new WMValueDigest(wm.getId(),
                settings.getCommandParams().getCentrifugeSpeed(),
                settings.getCommandParams().getTemp(),
                wm.getTemperatureUnit() == TemperatureUnit.CELSIUS? "C" : "F",
                settings.getCommandParams().getMode().toString());
    }

    public Thread getNewSimulatorThread() {
        Thread simulatorThread = new Thread(this);
        simulatorThread.start();
        return simulatorThread;
    }

    public void sendStateAndStatus(WMValueDigest state) {
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(state);
            int onOff = isOff ? 3 : 2;
            publishMessageLite(json);
            publishStatusMessageLite("status," + onOff + "T," + wm.getId());
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void sendInternalState() {
        try {
            String status = isOff? "OFF" : "ON";
            publishOnOff(status + "," + wm.getId());
        }
        catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void scheduleThread(WMCommand received) {
        long from = received.getCommandParams().getFrom();
        long init, stopTime, to;
        long nowMillis = new Date().getTime();
        to = from + modeInMinutesDuration.get(received.getCommandParams().getMode()) * 1000 ;//Lấy thời gian kết thúc của tác vụ
        if (from < nowMillis) {     // thời gian bắt đầu đã qua, + 24 giờ vào thời gian bắt đầu và kết thúc (from và to) để lên lịch cho ngày tiếp theo
            init = (from + 1000 * 60 * 60 * 24) - nowMillis;
            from += 1000 * 60 * 60 * 24;
            stopTime = to - nowMillis;
            to += 1000 * 60 * 60 * 24;
        } else {
            init = from - nowMillis;
            stopTime = to - nowMillis;
        }
        Runnable changeSettings = () -> {
            settings = received;
            isOff = false;
            publishStateMessage(new ACStateChange(0,
                    wm.getId(),
                    WMState.SCHEDULE_ON.toString(),null));
        };
        Long id = scheduledThreadCount.incrementAndGet();
        ScheduledFuture<?> once = scheduler.schedule(() -> {
            scheduler.schedule(
                    changeSettings,
                    0,
                    TimeUnit.MILLISECONDS);
            scheduler.schedule(() -> {
                scheduledThread.remove(id);
                scheduledDetails.remove(id);
                getSchedules();
                publishStateMessage(new ACStateChange(0,
                        wm.getId(),
                        WMState.SCHEDULE_OFF.toString(),null));
                isOff = true;
            }, stopTime, TimeUnit.MILLISECONDS);
        },init, TimeUnit.MILLISECONDS);

        scheduledThread.put(id, once);
        scheduledDetails.put(id, new Scheduled(id,from, to, false));
        Map<String,String> extraInfo = new HashMap<>();
        extraInfo.put("from",Long.toString(from));
        extraInfo.put("to", Long.toString(to));
        publishStateMessage(new ACStateChange(received.getCommandParams().getUserId(),
                wm.getId(),
                WMState.SCHEDULE.toString(),extraInfo));

    }

    private void removeScheduledThread(WMCommand received) {
        Long scheduledTaskId = received.getCommandParams().getTaskId();
        if(scheduledThread.containsKey(scheduledTaskId)) {
            scheduledThread.get(scheduledTaskId).cancel(true);
            scheduledThread.remove(scheduledTaskId);

            Map<String,String> extraInfo = new HashMap<>();
            extraInfo.put("from",Long.toString(scheduledDetails.get(scheduledTaskId).getFrom()));
            extraInfo.put("to", Long.toString(scheduledDetails.get(scheduledTaskId).getTo()));
            publishStateMessage(new ACStateChange(received.getCommandParams().getUserId(),
                    wm.getId(),
                    WMState.CANCEL_SCHEDULED.toString(),extraInfo));
            scheduledDetails.remove(scheduledTaskId);
        }
    }

    private void getSchedules() {
        List<Scheduled> schedules = new ArrayList<>();
        for (Map.Entry<Long, Scheduled> entry : scheduledDetails.entrySet()) {
            schedules.add(entry.getValue());
        }
        SchedulesPerUser schedulesPerUser = new SchedulesPerUser(wm.getId(),schedules);
        try {
            publishSchedulesLite(mapper.writeValueAsString(schedulesPerUser));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println(schedules);
    }

    private void publishMessageLite(String message) throws MqttException {
        this.mqttConfiguration.getClient().publish("wm", new MqttMessage(message.getBytes()));
    }

    private void publishSchedulesLite(String message) throws MqttException {
        this.mqttConfiguration.getClient().publish("scheduled", new MqttMessage(message.getBytes()));
        if(wm.getId() == id) {
            System.out.println("Sent SCHEDULES (" + scheduleOrdinal + ") - " + new Date());
            scheduleOrdinal += 1;
        }
    }

    private void publishOnOff(String message) throws MqttException {
        this.mqttConfiguration.getClient().publish("status/wm", new MqttMessage(message.getBytes()));
        if(wm.getId() == id) {
            System.out.println("Sending STATUS (" + onOffOrdinal + ") - " + new Date());
            onOffOrdinal += 1;
        }
    }

    private void publishStatusMessageLite(String message) throws MqttException {
        this.mqttConfiguration.getClient().publish("statuses", new MqttMessage(message.getBytes()));
    }

    private void publishStateMessage(ACStateChange state) {
        try {
            this.mqttConfiguration.getClient().publish("states", new MqttMessage(mapper.writeValueAsString(state).getBytes()));
            if(wm.getId() == id) {
                System.out.println("Sending " + sent + "(" + stateOrdinal + ") - " + new Date());
                stateOrdinal += 1;
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String getModeName(WMMode mode) {
        String modeName = String.valueOf(mode);
        if(mode == WMMode.DARK_WASH) {
            modeName = "DARK WASH";
        }
        else if(mode == WMMode.DAILY_EXPRESS) {
            modeName = "DAILY EXPRESS";
        }
        else if(mode == WMMode.RINSE_SPIN) {
            modeName = "RINSE & SPIN";
        }
        else if(mode == WMMode.SPIN_ONLY) {
            modeName = "SPIN ONLY";
        }
        return modeName;
    }
    private void publishOn(WMCommand receivedCommand) {
        Map<String,String> extraInfo = new HashMap<>();
        WMMode mode = receivedCommand.getCommandParams().getMode();
        String modeName = getModeName(mode);
        extraInfo.put("mode",modeName);
        extraInfo.put("temp",receivedCommand.getCommandParams().getTemp().toString());
        extraInfo.put("centrifuge", receivedCommand.getCommandParams().getCentrifugeSpeed().toString());
        publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                wm.getId(),
                WMState.ON.toString(),extraInfo));
    }
}
