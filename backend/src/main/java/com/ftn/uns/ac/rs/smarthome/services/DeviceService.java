package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Measurement;
import com.ftn.uns.ac.rs.smarthome.models.UserIdUsernamePair;
import com.ftn.uns.ac.rs.smarthome.models.devices.*;
import com.ftn.uns.ac.rs.smarthome.models.dtos.*;
import com.ftn.uns.ac.rs.smarthome.models.enums.ACState;
import com.ftn.uns.ac.rs.smarthome.models.enums.ChargerState;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DeviceService implements IDeviceService {
    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final InfluxService influxService;
    private final MessageSource messageSource;
    private final UserService userService;

    public DeviceService(DeviceRepository deviceRepository,
                         InfluxService influxService,
                         MessageSource messageSource,
                         UserService userService) {
        this.deviceRepository = deviceRepository;
        this.influxService = influxService;
        this.messageSource = messageSource;
        this.userService = userService;
    }

    @Override
    public Optional<Device> getById(Integer id) {
        return deviceRepository.findById(id);
    }

    @Override
    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    @Override
    @Cacheable(value = "devices", key = "'user-' + #ownerId")
    public List<DeviceDetailsDTO> findByOwnerId(Integer ownerId) {
        List<Device> ownersDevices =  deviceRepository.findByOwnerId(ownerId);
        List<DeviceDetailsDTO> devicesDetails = new ArrayList<>();
        for(Device device : ownersDevices) {
            String type = "";
            if(device instanceof Thermometer) type = "THERMOMETER";
            if (device instanceof SolarPanelSystem) type = "SPS";
            if (device instanceof AirConditioner) type = "AC";
            if (device instanceof WashingMachine) type = "WM";
            if (device instanceof Charger) type = "CHARGER";
            DeviceDetailsDTO details = new DeviceDetailsDTO(
                    device.getId(),
                    type,
                    device.getName(),
                    device.getPowerSource(),
                    device.getEnergyConsumption(),
                    device.getImage(),
                    device.getProperty().getName()
            );
            devicesDetails.add(details);
        }
        return devicesDetails;
    }


    @Override
    public void update(Device device) {
        deviceRepository.save(device);
    }

    @Override
    public void setDeviceStillThere(int id) {
        log.info("Setting device with id {} to still there", id);
        Optional<Device> device = deviceRepository.findById(id);
        if (device.isEmpty()) {
            log.error("Device with id {} not found", id);
            return;
        }
        device.get().setStillThere(true);
        deviceRepository.save(device.get());
    }

    @Override
    public List<List<Measurement>> getStreamByMeasurementNameAndDeviceIdInTimeRange(MeasurementsStreamRequestDTO requestDTO) {
        if(requestDTO.getFrom() >= requestDTO.getTo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("dateRange.invalid", null, Locale.getDefault()));
        }
        if(requestDTO.getDeviceId() != -1 && deviceRepository.findById(requestDTO.getDeviceId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("device.notFound", null, Locale.getDefault()));
        }
        int batchSize = 5000, page = 0;
        List<List<Measurement>> batches = new ArrayList<>();
        requestDTO.setLimit(batchSize);
        while(true) {
            requestDTO.setOffset(page * batchSize);
            MeasurementsDTO batch = influxService.findPaginatedByMeasurementNameAndDeviceIdInTimeRange(requestDTO);
            batches.add(batch.getBatch());
            if(!batch.isHasMore()) {
                break;}
            else
                page += 1;
        }
        return batches;
    }

    @Override
    public List<List<Measurement>> findPowerAggregation(PowerMeasurementsStreamRequestDTO requestDTO) {
        if(requestDTO.getFrom() >= requestDTO.getTo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("dateRange.invalid", null, Locale.getDefault()));
        }
        int batchSize = 5000, page = 0;
        List<List<Measurement>> batches = new ArrayList<>();
        requestDTO.setLimit(batchSize);
        while (true) {
            requestDTO.setOffset(page * batchSize);
            MeasurementsDTO batch = influxService.findPowerAggregation(requestDTO);
            batches.add(batch.getBatch());
            if(!batch.isHasMore()) {
                break;
            } else {
                page += 1;
            }
        }

        return batches;
    }

    @Override
    public CommandsDTO getCommandsByTimeRangeAndUserId(CommandsRequestDTO request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm:ss a");
        if(request.getFrom() >= request.getTo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("dateRange.invalid", null, Locale.getDefault()));
        }
        if(deviceRepository.findById(request.getDeviceId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("device.notFound", null, Locale.getDefault()));
        }
        List<CommandSummary> commands = new ArrayList<>();
        List<UserIdUsernamePair> allUsers = new ArrayList<>();
        List<CommandSummaryInternal> commandsInternal = influxService.findPaginatedByTimeSpanAndUserIdAndDeviceId(request);
        for(CommandSummaryInternal command : commandsInternal) {
            String commandDesc = "";
            if (command.getCommand().equals(ChargerState.START_CHARGE.toString())) {
                float carCharge = Float.parseFloat(command.getTags().get("carCharge"));
                float carCapacity = Float.parseFloat(command.getTags().get("carCapacity"));
                float percentage = (carCharge / carCapacity) * 100;
                String port = command.getTags().get("portNum");
                commandDesc += "Started charging at port " + port + ". Car currently at " + String.format("%.2f", percentage) + "%";
            } else if (command.getCommand().equals(ChargerState.END_CHARGE.toString())) {
                String port = command.getTags().get("portNum");
                float expendedEnergy = Float.parseFloat(command.getTags().get("spentEnergy"));
                commandDesc += "Stopped charging at port " + port + ". Expended " + String.format("%.2f", expendedEnergy) + " kWh";
            } else if(command.getCommand().equals(ACState.ON.toString())) {
                commandDesc = "Turned on the device";
                if(command.getTags().get("mode") != null) {
                    commandDesc += " (Mode: " + command.getTags().get("mode") + ", \nTemperature: " + command.getTags().get("temp") + ",\nCentrifuge Speed: " + command.getTags().get("centrifuge") + ")";
                }
            }
            else if(command.getCommand().equals(ACState.OFF.toString()))
                commandDesc = "Turned off the device";
            else if(command.getCommand().equals(ACState.HEAT_MODE.toString()))
                commandDesc = "Changed mode to heating";
            else if(command.getCommand().equals(ACState.COOL_MODE.toString()))
                commandDesc = "Changed mode to cooling";
            else if(command.getCommand().equals(ACState.DRY_MODE.toString()))
                commandDesc = "Changed mode to ventilation";
            else if(command.getCommand().equals(ACState.AUTO_MODE.toString()))
                commandDesc = "Changed mode to automatic";
            else if(command.getCommand().equals(ACState.FUNGUS_CHANGE.toString())) {
                if (command.getTags().get("isFungus").equals("true"))
                    commandDesc = "Turned on the fungus spreading prevention";
                else
                    commandDesc = "Turned off the fungus spreading prevention";
            }
            else if(command.getCommand().equals(ACState.HEALTH_CHANGE.toString())) {
                if (command.getTags().get("isHealth").equals("true"))
                    commandDesc = "Turned on the air ionizer";
                else
                    commandDesc = "Turned off the air ionizer";
            }
            else if(command.getCommand().equals(ACState.FAN_SPEED_CHANGE.toString()))
                commandDesc = "Changed fan speed to " + command.getTags().get("fanSpeed");
            else if(command.getCommand().equals(ACState.TEMP_CHANGE.toString()))
                commandDesc = "Changed target temperature to " + command.getTags().get("target");
            else if(command.getCommand().equals(ACState.SCHEDULE_ON.toString()))
                commandDesc = "Device turned on according to scheduled cycle";
            else if(command.getCommand().equals(ACState.SCHEDULE_OFF.toString()))
                commandDesc = "Device turned off according to scheduled cycle";
            else if(command.getCommand().equals(ACState.SCHEDULE.toString())) {

                commandDesc = "Scheduled cycle from " + formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(command.getTags().get("from"))), java.time.ZoneId.systemDefault())) +
                        " to " + formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(command.getTags().get("to"))), java.time.ZoneId.systemDefault()));

                if (command.getTags().get("everyDay") != null && command.getTags().get("everyDay").equals("true"))
                    commandDesc += " (repeats every day)";

            }
            else {
                commandDesc = "Cancelled the cycle scheduled from " + formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(command.getTags().get("from"))),java.time.ZoneId.systemDefault())) +
                        " to " + formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(command.getTags().get("to"))),java.time.ZoneId.systemDefault()));
                if (command.getTags().get("everyDay") != null && command.getTags().get("everyDay").equals("true"))
                    commandDesc += " (repeats every day)";
            }
            if(!command.getTags().get("userId").equals("0")) {
                UserInfoDTO user = userService.getUserInfo(Integer.parseInt(command.getTags().get("userId")));
                commands.add(new CommandSummary(command.getTimestamp().getTime(), user.getUsername() + " (" + user.getName() +  " " + user.getSurname() +")", commandDesc));
            }
            else {
                commands.add(new CommandSummary(command.getTimestamp().getTime(), "Device", commandDesc));
            }
        }
        if(request.getFirstFetch()) {
            List<Integer> allUserIds = this.influxService.findAllDistinctUsersForAllRecords(request.getDeviceId());
            for(Integer id : allUserIds) {
                if(id == 0) continue;
                UserInfoDTO userInfo = this.userService.getUserInfo(id);
                allUsers.add(new UserIdUsernamePair(id,userInfo.getUsername() + " (" + userInfo.getName() +  " " + userInfo.getSurname() +")"));
            }
            allUsers.add(new UserIdUsernamePair(0,"Device"));
        }
        return new CommandsDTO(commands,allUsers);
    }
}
