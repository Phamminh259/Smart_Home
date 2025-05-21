package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.DeviceControl;
import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.UserSearchInfo;
import com.ftn.uns.ac.rs.smarthome.models.devices.*;
import com.ftn.uns.ac.rs.smarthome.models.dtos.DeviceControlDetails;
import com.ftn.uns.ac.rs.smarthome.models.dtos.DeviceDetailsDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceControlRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IDeviceControlService;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IDeviceService;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IPropertyService;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IUserService;
import org.slf4j.Logger;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.Null;
import java.util.*;

@Service
public class DeviceControlService implements IDeviceControlService {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private final DeviceControlRepository deviceControlRepository;
    private final IDeviceService deviceService;
    private final MessageSource messageSource;
    private final IPropertyService propertyService;
    private final IUserService userService;
    private final CacheManager cacheManager;

    public DeviceControlService(DeviceControlRepository deviceControlRepository,
                                IDeviceService deviceService,
                                MessageSource messageSource,
                                IPropertyService propertyService,
                                IUserService userService,
                                CacheManager cacheManager) {
        this.deviceControlRepository = deviceControlRepository;
        this.deviceService = deviceService;
        this.messageSource = messageSource;
        this.propertyService = propertyService;
        this.userService = userService;
        this.cacheManager = cacheManager;
    }

    @Override
    public void editDeviceControl(Integer deviceId, List<DeviceControlDetails> details) {
        Optional<Device> device = deviceService.getById(deviceId);
        if(device.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("device.notFound", null, Locale.getDefault()));
        }
        for(DeviceControlDetails detail : details) {
            Optional<User> user = userService.getById(detail.getUserId());
            Optional<DeviceControl>dc = deviceControlRepository.findByDevice_IdAndOwner_Id(deviceId, detail.getUserId());
            if(user.isEmpty()){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("user.notFound", null, Locale.getDefault()));
            }
            if(detail.getAction().equals("a")) {
                if(dc.isEmpty()) {
                    deviceControlRepository.save(new DeviceControl(user.get(), device.get()));
                    evictCacheForUser(user.get().getId());
                }
            }
            if(detail.getAction().equals("d")) {
                dc.ifPresent(deviceControlRepository::delete);
                evictCacheForUser(detail.getUserId());
            }
        }
    }

    @Override
    public void editDeviceControlForProperty(Integer propertyId, List<DeviceControlDetails> details) {
        Optional<Property> property = propertyService.getById(propertyId);
        if(property.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("property.notFound", null, Locale.getDefault()));
        }

        for (DeviceControlDetails detail : details) {
            Optional<User> user = userService.getById(detail.getUserId());
            if (user.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("user.notFound", null, Locale.getDefault()));
            }
            if (detail.getAction().equals("a")) {
                for(Device device : property.get().getDevices()) {
                    if(deviceControlRepository.findByDevice_IdAndOwner_Id(device.getId(), user.get().getId()).isEmpty()) {
                        deviceControlRepository.save(new DeviceControl(user.get(), device));
                        evictCacheForUser(user.get().getId());
                    }
                }
            }
            if (detail.getAction().equals("d")) {
                for(Device device : property.get().getDevices()) {
                    Optional<DeviceControl> dc = deviceControlRepository.findByDevice_IdAndOwner_Id(device.getId(), detail.getUserId());
                    dc.ifPresent(deviceControlRepository::delete);
                    evictCacheForUser(detail.getUserId());
                }
            }
        }
    }

    public void evictCacheForUser(Integer userId) {
//        log.info("Evicting cache for user with id: " + userId);
        try {
            Objects.requireNonNull(cacheManager.getCache("sharedDevices")).evict(userId);
        } catch (NullPointerException e) {
            log.error("sharedDevices cache not found for user with id: " + userId);
        }
//        log.info("Cache evicted for user with id: " + userId);
    }

    @Override
    public List<UserSearchInfo> getDeviceControlUserInfo(Integer deviceId) {
        List<UserSearchInfo> list = new ArrayList<>();
        Optional<Device> device = deviceService.getById(deviceId);
        if(device.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("device.notFound", null, Locale.getDefault()));
        }
        List<DeviceControl> controls = deviceControlRepository.findByDevice_Id(deviceId);
        for(DeviceControl control : controls) {
            list.add(new UserSearchInfo(control.getOwner().getId(), control.getOwner().getUsername(),
                    control.getOwner().getName() + " " + control.getOwner().getSurname()));
        }
        return list;
    }

    @Override
    @Cacheable(value = "sharedDevices", key = "#userId")
    public List<DeviceDetailsDTO> findByShared(Integer userId) {
        List<DeviceDetailsDTO> devices = new ArrayList<>();
        List<DeviceControl> shared = deviceControlRepository.findByOwner_Id(userId);
        for(DeviceControl control : shared) {
            Device device = control.getDevice();
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
            devices.add(details);
        }
        return devices;
    }

    @Override
    public List<UserSearchInfo> getDeviceControlUserInfoForProperty(Integer propertyId) {
        List<UserSearchInfo> list = new ArrayList<>();
        Optional<Property> property = propertyService.getById(propertyId);
        if(property.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("property.notFound", null, Locale.getDefault()));
        }
        Map<Integer, Integer> userDeviceControlCount = new HashMap<>();
        Map<Integer, UserSearchInfo> userInfo = new HashMap<>();
        for(DeviceControl control : deviceControlRepository.findByDevice_Property_Id(propertyId)) {
            Integer userId = control.getOwner().getId();
            userInfo.putIfAbsent(userId, new UserSearchInfo(userId, control.getOwner().getUsername(), control.getOwner().getName() + " " + control.getOwner().getSurname()));
            if(userDeviceControlCount.get(userId) == null) {
                userDeviceControlCount.put(userId, 1);
            }
            else {
                userDeviceControlCount.put(userId, userDeviceControlCount.get(userId) + 1);
            }
        }
        for(Map.Entry<Integer, Integer> entry : userDeviceControlCount.entrySet()) {
            if(entry.getValue() == property.get().getDevices().size())
                list.add(userInfo.get(entry.getKey()));
        }
        return list;
    }
}
