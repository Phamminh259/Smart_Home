package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.models.Property;
import com.ftn.uns.ac.rs.smarthome.models.PropertyStatus;
import com.ftn.uns.ac.rs.smarthome.models.Town;
import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.dtos.PropertyDTO;
import com.ftn.uns.ac.rs.smarthome.repositories.PropertyRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.TownRepository;
import com.ftn.uns.ac.rs.smarthome.repositories.UserRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IPropertyService;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IUserService;
import org.slf4j.Logger;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class PropertyService implements IPropertyService {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private final MessageSource messageSource;
    private IUserService userService;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final TownRepository townRepository;
    private final CacheManager cacheManager;

    public PropertyService(MessageSource messageSource,
                           PropertyRepository propertyRepository,
                           IUserService userService,
                           UserRepository userRepository,
                           TownRepository townRepository,
                           CacheManager cacheManager) {
        this.messageSource = messageSource;
        this.propertyRepository = propertyRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.townRepository = townRepository;
        this.cacheManager = cacheManager;
    }

    public void registerProperty(PropertyDTO propertyDTO) {
        try {
            if (this.propertyRepository.findByAddress(propertyDTO.getAddress()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("address.alreadyUsed", null, Locale.getDefault()));
            }
            Optional<User> owner = this.userRepository.findByUsername(propertyDTO.getOwner());
            if (owner.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("user.notExisting", null, Locale.getDefault()));
            }
            Optional<Town> town = townRepository.findByName(propertyDTO.getCity());
            if (town.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("town.notExisting", null, Locale.getDefault()));
            }
//            Path filepath = Paths.get("../temp/", propertyDTO.getPicture().getOriginalFilename());
//            propertyDTO.getPicture().transferTo(filepath);
//            File file = new File(filepath.toString());
//            File compressed = ImageCompressor.compressImage(file, 0.1f, propertyDTO.getOwner());
//            String[] tokens = compressed.getName().split("/");
//            String key = tokens[tokens.length - 1];
//            String type = propertyDTO.getPicture().getContentType();
//            String bucket = "images";
//            String pathToImage = "http://127.0.0.1:9000/" + bucket + '/' + "profilePictures/" + key;
            String pathToImage = "";
            Property propertyToSave = new Property(propertyDTO.getAddress(),
                    propertyDTO.getSize(),
                    pathToImage,
                    owner.get(),
                    propertyDTO.getFloors(),
                    propertyDTO.getPropertyType(), this.propertyRepository.count());
            this.propertyRepository.save(propertyToSave);
            List<Property> list = town.get().getProperties();
            list.add(propertyToSave);
            town.get().setProperties(list);
            this.townRepository.save(town.get());
        } catch (ResponseStatusException ex) {
            throw ex;
        }
//         catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    @Cacheable(value = "approvedProperties", key = "#username")
    public List<PropertyDTO> getApprovedProperties(String username) {
        Optional<User> owner = userRepository.findByUsername(username);
        if (owner.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("user.notExisting", null, Locale.getDefault()));
        }
        List<Property> properties = propertyRepository.findAllByOwner(owner.get());
        List<PropertyDTO> propertyDTOS = new ArrayList<>();
        List<Town> towns = townRepository.findAll();
        for (Property property : properties) {
            for (Town town : towns) {
                if (town.getProperties().contains(property) && property.getStatus() == PropertyStatus.APPROVED) {
                    propertyDTOS.add(new PropertyDTO(property.getAddress(), property.getName(), town.getName(), property.getSize(), property.getFloors(), property.getStatus(), property.getPropertyType(), property.getOwner().getUsername(), property.getId()));
                }
            }
        }
        return propertyDTOS;
    }

    @Override
    public Object getAllProperty() {
        List<Property> properties = propertyRepository.findAll();
        List<PropertyDTO> propertyDTOS = new ArrayList<>();
        List<Town> towns = townRepository.findAll();
        for (Property property : properties) {
            for (Town town : towns) {
                if (town.getProperties().contains(property)) {
                    propertyDTOS.add(new PropertyDTO(property.getAddress(), property.getName(), town.getName(), property.getSize(), property.getFloors(), property.getStatus(), property.getPropertyType(), property.getOwner().getUsername(), property.getId()));
                }
            }
        }
        return propertyDTOS;
    }

    @Override
    public List<PropertyDTO> getAllApprovedProperties() {
        List<Property> properties = propertyRepository.findAll();
        List<PropertyDTO> propertyDTOS = new ArrayList<>();
        List<Town> towns = townRepository.findAll();
        for (Property property : properties) {
            if (!property.getStatus().equals(PropertyStatus.APPROVED)) {
                continue;
            }
            for (Town town : towns) {
                if (town.getProperties().contains(property)) {
                    propertyDTOS.add(new PropertyDTO(property.getAddress(), property.getName(), town.getName(), property.getSize(), property.getFloors(), property.getStatus(), property.getPropertyType(), property.getOwner().getUsername(), property.getId()));
                }
            }
        }
        return propertyDTOS;
    }

    @Override
    public void approveProperty(Integer id) {
        Optional<Property> property = propertyRepository.findById(id);
        if(property.isPresent()){
            property.get().setStatus(PropertyStatus.APPROVED);
            propertyRepository.save(property.get());
            evictCacheForProperty(property.get().getOwner().getUsername());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("property.notExisting", null, Locale.getDefault()));
        }
    }

    private void evictCacheForProperty(String username) {
//        logger.info("Evicting cache for user: " + username);
        try {
            Objects.requireNonNull(cacheManager.getCache("approvedProperties")).evict(username);
        } catch (NullPointerException e) {
            logger.error("approvedProperties cache not found for user: " + username);
        }
//        logger.info("Cache evicted for user: " + username);
    }

    @Override
    public void denyProperty(Integer id) {
        Optional<Property> property = propertyRepository.findById(id);
        if(property.isPresent()){
            property.get().setStatus(PropertyStatus.DENIED);
            propertyRepository.save(property.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("property.notExisting", null, Locale.getDefault()));
        }
    }

    @Override
    public Object getAllUnapprovedProperty() {
        List<Property> properties = propertyRepository.findAllByStatus(PropertyStatus.UNAPPROVED);
        List<PropertyDTO> propertyDTOS = new ArrayList<>();
        List<Town> towns = townRepository.findAll();
        for (Property property : properties) {
            for (Town town : towns) {
                if (town.getProperties().contains(property)) {
                    propertyDTOS.add(new PropertyDTO(property.getAddress(), property.getName(), town.getName(), property.getSize(), property.getFloors(), property.getStatus(), property.getPropertyType(), property.getOwner().getUsername(), property.getId()));
                }
            }
        }
        return propertyDTOS;
    }

    @Override
    public Optional<Property> getById(Integer id) {
        return propertyRepository.findById(id);
    }

    @Override
    public List<Integer> getPropertyIdsByCityId(Integer id) {
        Optional<Town> town = townRepository.findById(id);
        if (town.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("town.notExisting", null, Locale.getDefault()));
        }
        List<Property> properties = town.get().getProperties();
        logger.info(properties.toString());
        List<Integer> ids = new ArrayList<>();
        for (Property property : properties) {
            ids.add(property.getId());
        }
        return ids;
    }
}