package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.config.MqttConfiguration;
import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.PropertyStatus;
import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.devices.Device;
import com.ftn.uns.ac.rs.smarthome.models.dtos.devices.DeviceDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.DeviceRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.PropertyRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IGenericDeviceService;
import com.ftn.uns.ac.rs.smarthome.utils.ImageCompressor;
import com.ftn.uns.ac.rs.smarthome.utils.S3API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.MessageSource;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Properties;

public abstract class GenericDeviceService<D extends Device, DDTO extends DeviceDTO> implements IGenericDeviceService<D, DDTO> {
    protected static final Logger log = LoggerFactory.getLogger(GenericDeviceService.class);
    protected final PropertyRepository propertyRepository;
    protected final DeviceRepository deviceRepository;
    protected final MessageSource messageSource;
    protected final S3API fileServerService;
    protected final Properties env;

    public GenericDeviceService(PropertyRepository propertyRepository,
                                DeviceRepository deviceRepository,
                                MessageSource messageSource,
                                S3API fileServerService) throws IOException {
        this.propertyRepository = propertyRepository;
        this.deviceRepository = deviceRepository;
        this.messageSource = messageSource;
        this.fileServerService = fileServerService;
        this.env = new Properties();
        env.load(MqttConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));
    }

    @Override
    @CacheEvict(value = "devices", key = "'user-' + #user.id")
    public void register(@Valid DDTO dto, User user) throws IOException {
        Optional<Property> property = propertyRepository.findByName(dto.getPropertyId().toString());
        if (property.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("property.notFound", null, Locale.getDefault()));
        }

        if (!property.get().getOwner().equals(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("property.notOwner", null, Locale.getDefault()));
        }

        if (!property.get().getStatus().equals(PropertyStatus.APPROVED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("property.notApproved", null, Locale.getDefault()));
        }

        D device = createDevice(dto, property.get());
        D savedDevice = deviceRepository.save(device);

        log.info(dto.getClass().getSimpleName() + " registered: " + savedDevice.getName());
        String path = env.getProperty("tempfolder.path");
//        Path filepath = Paths.get(path, dto.getImage().getOriginalFilename());
        Path filepath = Paths.get(path, "devices", dto.getImage().getOriginalFilename());
        try {
            dto.getImage().transferTo(filepath);
        } catch (Exception e) {
            log.error("Error while uploading image: {}", e.getMessage());
            deviceRepository.deleteById(savedDevice.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("image.upload.failed", null, Locale.getDefault()));
        }
        File file = new File(filepath.toString());
        File compressed;
        try {
            compressed = ImageCompressor.compressImage(file, 0.4f, "d" + savedDevice.getId());
        } catch (ResponseStatusException e) {
            log.error("Error while compressing image: {}", e.getMessage());
            deviceRepository.deleteById(savedDevice.getId());
            throw e;
        }

        String[] tokens = compressed.getName().split("/");
        String key = tokens[tokens.length - 1];
        String type = dto.getImage().getContentType();
        String bucket = "images";
        fileServerService.put(bucket, "devices/" + key, compressed, type).thenApply(lol ->
                        compressed.delete())
                .exceptionally(e -> false);
        String pathToImage = "http://localhost:8080/" + bucket + '/' + "devices/" + key;
        savedDevice.setImage(pathToImage);
        deviceRepository.save(savedDevice);
    }




    protected abstract D createDevice(DDTO dto, Property property);
}
