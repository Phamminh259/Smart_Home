package com.ftn.uns.ac.rs.smarthomesimulator.threads;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.ftn.uns.ac.rs.smarthomesimulator.config.MqttConfiguration;
import com.ftn.uns.ac.rs.smarthomesimulator.models.*;
import com.ftn.uns.ac.rs.smarthomesimulator.models.devices.AirConditioner;
import com.ftn.uns.ac.rs.smarthomesimulator.models.enums.*;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import java.util.concurrent.TimeUnit;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
// điều khiển thiết bị AirConditioner giao tiếp với MQTT Broker để gửi/nhận trạng thái
public class ACThread implements Runnable {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(ACThread.class);
    private double currentTemp;
    private final AirConditioner ac;
    private final ObjectMapper mapper = new ObjectMapper();
    private ACCommand settings;
    private boolean isOff = true;
    private MqttConfiguration mqttConfiguration;
    private boolean hasConfigChanged = true;
    private int onOffOrdinal = 1;
    private int changeOrdinal = 1;
    private int stateOrdinal = 1;
    private int schedulesOrdinal = 1;
    private final Map<Long, ScheduledFuture<?>> scheduledThread = new ConcurrentHashMap<>();
    private final Map<Long,Scheduled> scheduledDetails = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(5);
    private final AtomicLong scheduledThreadCount = new AtomicLong(0);
    private final int INTERVAL = 5;
    private double powerConsumption = -1;
    private final int id = 1001;

    private class MqttACMessageCallback implements MqttCallback {

        @Override
        public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {}

        @Override
        public void mqttErrorOccurred(MqttException e) {System.err.println(e.getMessage());}

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                hasConfigChanged = true;
                String message = new String(mqttMessage.getPayload());
                ACCommand receivedCommand = mapper.readValue(message, ACCommand.class);
                CommandType commandType = receivedCommand.getCommandType();
                if(commandType == CommandType.ON || commandType == CommandType.CHANGE) {
                    if(commandType == CommandType.CHANGE) {
                        publishChanges(receivedCommand);
                        if(ac.getId() == 1001) {
                            System.out.println("Received CHANGE (" + changeOrdinal + ") - " + new Date());
                            changeOrdinal += 1;
                        }
                    }
                    else {
                        if(ac.getId() == 1001) {
                            System.out.println("Received ON (" + onOffOrdinal + ") - " + new Date());
                            onOffOrdinal += 1;
                        }
                        publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                                ac.getId(),
                                ACState.ON.toString(),null));
                    }
                    isOff = false;
                    settings = receivedCommand;
                }
                else if(commandType == CommandType.OFF){
                    if(ac.getId() == 1001) {
                        System.out.println("Received OFF (" + onOffOrdinal + ") - " + new Date());
                        onOffOrdinal += 1;
                    }
                    publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                            ac.getId(),
                            ACState.OFF.toString(),null));
                    isOff = true;
                }
                else if(commandType == CommandType.CANCEL_SCHEDULED) {
                    if(ac.getId() == 1001) {
                        System.out.println("Received CANCEL (" + schedulesOrdinal + ") - " + new Date());
                        schedulesOrdinal += 1;
                    }
                    removeScheduledThread(receivedCommand);
                    getSchedules();
                }
                else if(commandType == CommandType.GET_SCHEDULES) {
                    if(ac.getId() == 1001) {
                        System.out.println("Received GET SCHEDULES (" + schedulesOrdinal + ") - " + new Date());
                        schedulesOrdinal += 1;
                    }
                    getSchedules();
                }
                else {
                    if(ac.getId() == 1001) {
                        System.out.println("Received SCHEDULE (" + schedulesOrdinal + ") - " + new Date());
                        schedulesOrdinal += 1;
                    }
                    scheduleThread(receivedCommand);
                    getSchedules();
                }
            }
            catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        @Override
        public void deliveryComplete(IMqttToken iMqttToken) {}

        @Override
        public void connectComplete(boolean b, String s) {}

        @Override
        public void authPacketArrived(int i, MqttProperties mqttProperties) {}
    }
    public ACThread(AirConditioner ac,
                    ACCommand settings) {
        try {
            this.mqttConfiguration = new MqttConfiguration(new MqttACMessageCallback());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        this.ac = ac;
        this.settings = settings;
        currentTemp = 0;
        this.powerConsumption = INTERVAL * 2.0 * ac.getEnergyConsumption() / (60 * 60); // tiêu thụ giây
    }
// Subscribe lắng nghe lệnh
    @Override
    public void run() {
        try {
            mqttConfiguration.getClient().subscribe("command/ac/" + ac.getId(), 2);
            generateValues();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            //System.err.println("Simulator thread interrupted");
        }
        catch(MqttException ex) {
            //System.err.println("MQTT Error: " + ex.getMessage());
        }
    }
    public void generateValues() throws InterruptedException {
        String[] seasons = {
                "WIN", // Tháng 1
                "SPR", // Tháng 2
                "SPR", // Tháng 3
                "SPR", // Tháng 4
                "SUM", // Tháng 5
                "SUM", // Tháng 6
                "SUM", // Tháng 7
                "FAL", // Tháng 8
                "FAL", // Tháng 9
                "FAL", // Tháng 10
                "WIN", // Tháng 11
                "WIN"  // Tháng 12
        };

        //khoảng nhiệt độ cho từng mùa trong năm
        int[][] typicalTempRange = new int[][] {
                {14, 20}, // WIN: Đông
                {20, 28}, // SPR: Xuân
                {28, 36}, // SUM: Hạ
                {24, 30}  // FAL: Thu
        };
        int count = 1;
        while(true) {
            sendInternalState();
            LocalDateTime now = LocalDateTime.now();
            //xác định mùa dựa vào tháng hiện tại
            String season = seasons[now.getMonthValue() - 1];
            int correctIndex = switch (season) {
                case "WIN" -> 0;  //0 cho mùa đông, 1 cho mùa xuân, 2 cho mùa hè, 3 cho mùa thu).
                case "SPR" -> 1;
                case "SUM" -> 2;
                default -> 3;
            };
            if (settings != null) {
                if(hasConfigChanged) {
                    if (settings.getCommandParams().getCurrentTemp() == -1) {
                        currentTemp = ThreadLocalRandom.current().nextInt(typicalTempRange[correctIndex][0], typicalTempRange[correctIndex][1] + 1);
                        if(settings.getCommandParams().getUnit().equals("F"))
                            currentTemp = (currentTemp * 9/5) + 32;
                    } else
                        currentTemp = settings.getCommandParams().getCurrentTemp();

                }
                ACValueDigest value;
                if (!isOff) {
                    value = generateValue(settings.getCommandParams().getTarget(),
                            settings.getCommandParams().getFanSpeed(),
                            settings.getCommandParams().getMode(),
                            settings.getCommandParams().isHealth(),
                            settings.getCommandParams().isFungus());
                    sendStateAndStatus(value);
                    if (ac.getPowerSource().equals(PowerSource.HOUSE)) {
                        if (count % 2 == 0) {
                            sendPowerConsumption();
                        }
                        count++;
                    }
                }
                hasConfigChanged = false;
            }
            Thread.sleep(INTERVAL * 1000);
        }

    }

    private void sendPowerConsumption() {
        String message = "consumed," + powerConsumption + "p," + ac.getId() + "," + ac.getProperty().getId();
        //log.info("Sending power consumption: " + message);
        try {
            this.mqttConfiguration.getClient().publish("consumed", new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
            //log.error("Error while sending power consumption: " + e.getMessage());
        }
    }

    public Thread getNewSimulatorThread() {
        Thread simulatorThread = new Thread(this);
        simulatorThread.start();
        return simulatorThread;
    }

// tính nhiệt độ
    private ACValueDigest generateValue(int targetTemp, int fanSpeed,
                                       ACMode mode, boolean isIonizing,
                                       boolean preventFungus) {
        boolean isCelsius = settings.getCommandParams().getUnit().equals("C");
        // Mỗi đơn vị tốc độ quạt sẽ làm thay đổi nhiệt độ
        double tempChangePerInterval = isCelsius ? fanSpeed * 0.04 : fanSpeed * 0.15;
        if(mode == ACMode.HEAT) {
            if (currentTemp < targetTemp) {
                currentTemp += tempChangePerInterval;
            }
            if(currentTemp > targetTemp) {
               currentTemp = targetTemp;
            }
        }
        if(mode == ACMode.COOL) {
            if (currentTemp > targetTemp)
                currentTemp -= tempChangePerInterval;
            if(currentTemp < targetTemp) {
                currentTemp = targetTemp;
            }
        }
        if(mode == ACMode.AUTO) {
            if(currentTemp < targetTemp) {
                currentTemp += tempChangePerInterval;
                if(currentTemp > targetTemp)
                    currentTemp = targetTemp;
            }
            if (currentTemp > targetTemp) {
                currentTemp -= tempChangePerInterval;
                if(currentTemp < targetTemp)
                    currentTemp = targetTemp;
            }
        }
        //currentTemp -= (0.01 + Math.random() * (0.02 - 0.01)); //random temp dropout
        //System.out.println(currentTemp);
        return new ACValueDigest(ac.getId(),
                currentTemp,
                settings.getCommandParams().getTarget(),
                ac.getTemperatureUnit() == TemperatureUnit.CELSIUS? "C" : "F",
                mode.toString(),fanSpeed,isIonizing,preventFungus);
    }

    public void sendStateAndStatus(ACValueDigest state) {
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(state);
            int onOff = isOff ? 3 : 2;
            publishMessageLite(json);
            publishStatusMessageLite("status," + onOff + "T," + ac.getId());
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void sendInternalState() {
        try {
            String status = isOff? "OFF" : "ON";
            publishOnOff(status + "," + ac.getId());
        }
        catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void scheduleThread(ACCommand received) {
        long from = received.getCommandParams().getFrom();
        long to = received.getCommandParams().getTo();
        long init, stopTime;
        long nowMillis = new Date().getTime();
        if (from < nowMillis) {
            // nếu tg bé hơn ngày hiện tại
            init = (from + 1000 * 60 * 60 * 24) - nowMillis;   // 1 ngày = 1000 mili giây * 60 giây * 60 phút * 24 giờ
            from += 1000 * 60 * 60 * 24;  // 1000 * 60 * 60 * 24 = số milliseconds của 1 ngày = 86_400_000 (24h)
            stopTime = (to + 1000 * 60 * 60 * 24) - nowMillis; // công thêm 1 ngày của tg kết thúc
            to += 1000 * 60 * 60 * 24; // 1 ngày = 1000 mili giây * 60 giây * 60 phút * 24 giờ
        } else {
            init = from - nowMillis;
            stopTime = to - nowMillis;
        }



        Runnable changeSettings = () -> {
            settings = received;
            isOff = false;
            publishStateMessage(new ACStateChange(0,
                    ac.getId(),
                    ACState.SCHEDULE_ON.toString(),null));
        };
        if (received.getCommandParams().isEveryDay()) {
            ScheduledFuture<?> everyDay = scheduler.scheduleWithFixedDelay(() -> {
                scheduler.schedule(
                        changeSettings,
                        0,
                        TimeUnit.MILLISECONDS);
                scheduler.schedule(() -> {
                    isOff = true;
                    publishStateMessage(new ACStateChange(0,
                            ac.getId(),
                            ACState.SCHEDULE_OFF.toString(),null));
                }, stopTime, TimeUnit.MILLISECONDS);
            }, init, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
            Long id = scheduledThreadCount.incrementAndGet();
            scheduledThread.put(id, everyDay);
            scheduledDetails.put(id, new Scheduled(id,from, to, received.getCommandParams().isEveryDay()));
        } else {
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
                            ac.getId(),
                            ACState.SCHEDULE_OFF.toString(),null));
                    isOff = true;
                }, stopTime, TimeUnit.MILLISECONDS);
            },init, TimeUnit.MILLISECONDS);

            scheduledThread.put(id, once);
            scheduledDetails.put(id, new Scheduled(id,from, to, received.getCommandParams().isEveryDay()));
            Map<String,String> extraInfo = new HashMap<>();
            extraInfo.put("from",Long.toString(from));
            extraInfo.put("to",Long.toString(to));
            extraInfo.put("everyDay", String.valueOf(received.getCommandParams().isEveryDay()));
            publishStateMessage(new ACStateChange(received.getCommandParams().getUserId(),
                    ac.getId(),
                    ACState.SCHEDULE.toString(),extraInfo));
        }
    }

    private void removeScheduledThread(ACCommand received) {
        Long scheduledTaskId = received.getCommandParams().getTaskId();
        if(scheduledThread.containsKey(scheduledTaskId)) {
            scheduledThread.get(scheduledTaskId).cancel(true);
            scheduledThread.remove(scheduledTaskId);

            Map<String,String> extraInfo = new HashMap<>();
            extraInfo.put("from",Long.toString(scheduledDetails.get(scheduledTaskId).getFrom()));
            extraInfo.put("to",Long.toString(scheduledDetails.get(scheduledTaskId).getTo()));
            extraInfo.put("everyDay", String.valueOf(scheduledDetails.get(scheduledTaskId).isEveryDay()));
            publishStateMessage(new ACStateChange(received.getCommandParams().getUserId(),
                    ac.getId(),
                    ACState.CANCEL_SCHEDULED.toString(),extraInfo));
            scheduledDetails.remove(scheduledTaskId);
        }
    }

    private void getSchedules() {
        List<Scheduled> schedules = new ArrayList<>();
        for (Map.Entry<Long, Scheduled> entry : scheduledDetails.entrySet()) {
           schedules.add(entry.getValue());
        }
        SchedulesPerUser schedulesPerUser = new SchedulesPerUser(ac.getId(),schedules);
        try {
            publishSchedulesLite(mapper.writeValueAsString(schedulesPerUser));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void publishMessageLite(String message) throws MqttException {
        this.mqttConfiguration.getClient().publish("ac", new MqttMessage(message.getBytes()));
    }

    private void publishSchedulesLite(String message) throws MqttException {
        this.mqttConfiguration.getClient().publish("scheduled", new MqttMessage(message.getBytes()));
        if(ac.getId() == id) {
            System.out.println("Sent SCHEDULES (" + schedulesOrdinal + ") - " + new Date());
            schedulesOrdinal += 1;
        }
    }

    private void publishOnOff(String message) throws MqttException {
        this.mqttConfiguration.getClient().publish("status/ac", new MqttMessage(message.getBytes()));
        if(ac.getId() == 1001) {
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
            if(ac.getId() == id) {
                System.out.println("Sending " + state.getChange() + " (" + stateOrdinal + ") - " + new Date());
                stateOrdinal += 1;
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    private void publishChanges(ACCommand receivedCommand) {
        int changedTemp, changedFanSpeed;
        if(receivedCommand.getCommandParams().isHealth() != settings.getCommandParams().isHealth()) {
            Map<String,String> extraInfo = new HashMap<>();
            extraInfo.put("isHealth",String.valueOf(receivedCommand.getCommandParams().isHealth()));
            publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                    ac.getId(),
                    ACState.HEALTH_CHANGE.toString(),extraInfo));
        }
        if(receivedCommand.getCommandParams().isFungus() != settings.getCommandParams().isFungus()) {
            Map<String,String> extraInfo = new HashMap<>();
            extraInfo.put("isFungus",String.valueOf(receivedCommand.getCommandParams().isFungus()));
            publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                    ac.getId(),
                    ACState.FUNGUS_CHANGE.toString(),extraInfo));
        }
        if(receivedCommand.getCommandParams().getMode() != settings.getCommandParams().getMode()) {
            if (receivedCommand.getCommandParams().getMode() == ACMode.AUTO) {
                publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                        ac.getId(),
                        ACState.AUTO_MODE.toString(),null));
            }
            if (receivedCommand.getCommandParams().getMode() == ACMode.COOL) {
                publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                        ac.getId(),
                        ACState.COOL_MODE.toString(),null));
            }
            if(receivedCommand.getCommandParams().getMode() == ACMode.HEAT) {
                publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                        ac.getId(),
                        ACState.HEAT_MODE.toString(),null));
            }
            if(receivedCommand.getCommandParams().getMode() == ACMode.DRY) {
                publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                        ac.getId(),
                        ACState.DRY_MODE.toString(),null));
            }
        }
        if(!Objects.equals(receivedCommand.getCommandParams().getTarget(), settings.getCommandParams().getTarget())) {
            changedTemp = receivedCommand.getCommandParams().getTarget();
            Map<String,String> extraInfo = new HashMap<>();
            extraInfo.put("target",String.valueOf(changedTemp));
            publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                    ac.getId(),
                    ACState.TEMP_CHANGE.toString(),extraInfo));
        }
        if(!Objects.equals(receivedCommand.getCommandParams().getFanSpeed(), settings.getCommandParams().getFanSpeed())) {
            changedFanSpeed = receivedCommand.getCommandParams().getFanSpeed();
            Map<String,String> extraInfo = new HashMap<>();
            extraInfo.put("fanSpeed",String.valueOf(changedFanSpeed));
            publishStateMessage(new ACStateChange(receivedCommand.getCommandParams().getUserId(),
                    ac.getId(),
                    ACState.FAN_SPEED_CHANGE.toString(),extraInfo));
        }
    }

}
