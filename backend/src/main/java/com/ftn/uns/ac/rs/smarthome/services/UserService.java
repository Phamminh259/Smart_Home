package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.config.MqttConfiguration;
import com.ftn.uns.ac.rs.smarthome.models.Role;
import com.ftn.uns.ac.rs.smarthome.models.User;
import com.ftn.uns.ac.rs.smarthome.models.UserSearchInfo;
import com.ftn.uns.ac.rs.smarthome.models.dtos.*;
import com.ftn.uns.ac.rs.smarthome.repositories.UserRepository;
import com.ftn.uns.ac.rs.smarthome.services.interfaces.IUserService;
import com.ftn.uns.ac.rs.smarthome.utils.ImageCompressor;
import com.ftn.uns.ac.rs.smarthome.utils.S3API;
import com.ftn.uns.ac.rs.smarthome.utils.SuperadminPasswordGenerator;
import com.ftn.uns.ac.rs.smarthome.utils.TokenUtils;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final SuperadminPasswordGenerator passwordGenerator;
    private final MessageSource messageSource;
    private final TokenUtils tokenUtils;
    private final S3API fileServerService;
    private final MailService mailService;
    private final Properties env;
    private BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public UserService(UserRepository userRepository,
                       RoleService roleService,
                       SuperadminPasswordGenerator passwordGenerator,
                       MessageSource messageSource,
                       TokenUtils tokenUtils,
                       S3API fileServerService,
                       MailService mailService) throws IOException {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordGenerator = passwordGenerator;
        this.messageSource = messageSource;
        this.tokenUtils = tokenUtils;
        this.fileServerService = fileServerService;
        this.mailService = mailService;
        this.env = new Properties();
        env.load(MqttConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return user.get();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void generateSuperadminAccount() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            String randomPassword = passwordGenerator.generateSuperadminPassword();
            String hashedRandomPassword = passwordEncoder().encode(randomPassword);

            Optional<Role> roleOptional = roleService.getByName("ROLE_ SUPERADMIN");
            if (roleOptional.isPresent()) {
                List<Role> role = List.of(roleOptional.get());

                String profilePictureLocation = "http://localhost:8080/images/profilePictures/admin.jpg";
                User superadmin = new User(3,
                        "admin",
                        "Admin",
                        "Admin",
                        "admin admin admin",
                        "smarthome.superadmin@no-reply.com",
                        hashedRandomPassword,
                        false,
                        profilePictureLocation,
                        role
                );
                userRepository.save(superadmin);

                try {
                    String path = env.getProperty("tempfolder.path");
                    PrintWriter writer = new PrintWriter(path + "/pwfile.txt", StandardCharsets.UTF_8);
                    writer.println(randomPassword);
                    writer.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            } else {
                // Xử lý trường hợp không tìm thấy ROLE_SUPERADMIN
                System.out.println("Role 'ROLE_SUPERADMIN' not found.");
            }
        }
    }
//@EventListener(ApplicationReadyEvent.class)
//public void generateSuperadminAccount() {
//    if(userRepository.findByUsername("admin").isEmpty()) {
//        String randomPassword = passwordGenerator.generateSuperadminPassword();
//        String hashedRandomPassword = passwordEncoder().encode(randomPassword);
//        List<Role> role = List.of(roleService.getByName("ROLE_SUPERADMIN").get());
//        String profilePictureLocation = "http://127.0.0.1:9000/images/profilePictures/admin.jpg";
//        User superadmin = new User(3,
//                "admin",
//                "Admin",
//                "Admin",
//                "admin admin admin",
//                "smarthome.superadmin@no-reply.com",
//                hashedRandomPassword,
//                false,
//                profilePictureLocation,
//                role
//        );
//        userRepository.save(superadmin);
//
//        try {
//            String path = env.getProperty("tempfolder.path");
//            PrintWriter writer = new PrintWriter(path + "/pwfile.txt", StandardCharsets.UTF_8);
//            writer.println(randomPassword);
//            writer.close();
//        }
//        catch (IOException ex) {
//            System.out.println(ex.getMessage());
//        }
//    }
//}
//@EventListener(ApplicationReadyEvent.class)
//public void generateSuperadminAccount() {
//    // Kiểm tra xem user "admin" có tồn tại hay không
//    if (userRepository.findByUsername("admin").isEmpty()) {
//        // Tạo mật khẩu ngẫu nhiên cho superadmin
//        String randomPassword = passwordGenerator.generateSuperadminPassword();
//        String hashedRandomPassword = passwordEncoder.encode(randomPassword);
//
//        // Lấy role "ROLE_SUPERADMIN"
//        Optional<Role> superAdminRole = roleService.getByName("ROLE_SUPERADMIN");
//        if (superAdminRole.isPresent()) {
//            // Tạo user "superadmin"
//            List<Role> roles = List.of(superAdminRole.get());
//            String profilePictureLocation = "http://127.0.0.1:9000/images/profilePictures/admin.jpg";
//            User superadmin = new User(3,
//                    "admin",
//                    "Admin",
//                    "Admin",
//                    "admin admin admin",
//                    "smarthome.superadmin@no-reply.com",
//                    hashedRandomPassword,
//                    false,
//                    profilePictureLocation,
//                    roles
//            );
//            userRepository.save(superadmin);
//
//            // Lưu mật khẩu gốc vào file nếu có cấu hình thư mục tạm
//            String path = env.getProperty("tempfolder.path");
//            if (path == null) {
//                System.out.println("Đường dẫn thư mục tạm không được cấu hình!");
//            } else {
//                try {
//                    PrintWriter writer = new PrintWriter(path + "/pwfile.txt", StandardCharsets.UTF_8);
//                    writer.println(randomPassword);
//                    writer.close();
//                } catch (IOException ex) {
//                    System.out.println("Lỗi khi ghi file: " + ex.getMessage());
//                }
//            }
//        } else {
//            // Nếu không tìm thấy role "ROLE_SUPERADMIN"
//            System.out.println("Role ROLE_SUPERADMIN không tồn tại!");
//        }
//    } else {
//        System.out.println("Tài khoản admin đã tồn tại!");
//    }
//}
//}
    @Override
    public TokenDTO login(LoginDTO userInfo) {
        Optional<User> user = this.userRepository.findByUsername(userInfo.getUsername());
        if(user.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("user.notFound", null, Locale.getDefault()));
        if(!BCrypt.checkpw(userInfo.getPassword(),user.get().getPassword()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("login.invalid", null, Locale.getDefault()));
        Role userRole = user.get().getRoles().get(0);
        if(userRole.getName().equals("ROLE_SUPERADMIN") && !user.get().getIsConfirmed()) {
            return new TokenDTO(null,null);
        }
        if(!user.get().getIsConfirmed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("user.notActivated", null, Locale.getDefault()));
        }
        String token = tokenUtils.generateToken(userInfo.getUsername(), userRole);

        return new TokenDTO(token,tokenUtils.getExpirationDateFromToken(token).getTime());
    }

    @Override
    public void register(UserInfoRegister userInfo) {
        try {
            if(this.userRepository.findByUsername(userInfo.getUsername()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("username.alreadyUsed", null, Locale.getDefault()));
            }
            if(this.userRepository.findByEmail(userInfo.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("email.alreadyUsed", null, Locale.getDefault()));
            }
            Optional<Role> role = this.roleService.getByName(userInfo.getRole());
            if(role.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("role.notExisting", null, Locale.getDefault()));
            }
            Integer id = -1;
            String path = env.getProperty("tempfolder.path");
            String[] tokens = userInfo.getProfilePicture().getOriginalFilename().split("\\.");
//            Path filepath = Paths.get(path, userInfo.getUsername() + "." + tokens[tokens.length - 1]);
            Path filepath = Paths.get(path, "profilePictures", userInfo.getUsername() + "." + tokens[tokens.length - 1]);
            userInfo.getProfilePicture().transferTo(filepath);
            File file = new File(filepath.toString());
            String key = userInfo.getUsername() + "." + FilenameUtils.getExtension(file.getName());
            Thumbnails.of(file.getAbsolutePath()).scale(1.0f).outputQuality(0.4f).toFile(file);
            String type = userInfo.getProfilePicture().getContentType();
            String bucket = "images";
            String pathToImage = "http://localhost:8080/" + bucket + '/' + "profilePictures/" + key;
            User toSave = new User(
                    userInfo.getUsername(),
                    userInfo.getName(),
                    userInfo.getSurname(),
                    userInfo.getEmail(),
                    passwordEncoder().encode(userInfo.getPassword()), // băm trc khi lưu db
                    false,
                    pathToImage,
                    List.of(role.get())
                    );
            if(role.get().getName().equals("ROLE_ADMIN"))
                toSave.setIsConfirmed(true);
            User saved = this.userRepository.save(toSave);   // luu db
            id = saved.getId();
            if(role.get().getName().equals("ROLE_USER")) {
                String  mailMessage =
                """
               <html>
                 <head>
                   <link rel="preconnect" href="https://fonts.googleapis.com">
                   <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                   <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300&display=swap" rel="stylesheet">
                 </head>
                 <body style="font-family: 'Roboto', sans-serif;height:100%; color:black; display:flex; justify-content: center; align-items: center; padding:1.5em 10em; background-color:#1a1a1a">
                   <div style="width:100%;height:350px; background-color:white;text-align:center;border-radius:1.2em; ">
                     <h1 style="font-size:xxx-large;">Activation</h1>
                     <p style="font-size:x-large;">To activate your account, click on the button below:<p/>
                     <br>
                     <br>
                     <form action='http://localhost:8080/api/user/activate/""" + saved.getId() + "'" + """
                       >
                       <input type="submit" style="border-radius:1.2em; font-size:x-large; cursor:pointer !important; padding:1em 2em; background-color:#00ADB5; color:white; font-size:18px; font-family: Arial, Helvetica, sans-serif; border:none;" value="Activate"/>
                     </form>
                     <br>
                   </div>
                 </body>
               </html>
                """;
                boolean sentEmail = this.mailService.sendTextEmail(
                        userInfo.getEmail(),
                        "Account activation",
                        mailMessage
                );
                if (!sentEmail) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("activation.notSent", null, Locale.getDefault()));
                }
            }
            fileServerService.put(bucket, "profilePictures/" + key, file, type)
                    .thenApply(lol ->
                        file.delete()
                    )
                    .exceptionally(e -> false);
        }
        catch(IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("compression.error", null, Locale.getDefault()));
        }
        catch(ResponseStatusException ex) {
            throw ex;
        }
    }




    @Override
    public void activate(Integer userId) {
        Optional<User> user = this.userRepository.findById(userId);
        if(user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("user.notFound", null, Locale.getDefault()));
        }
        user.get().setIsConfirmed(true);
        this.userRepository.save(user.get());
    }

//    @Override
//    public void sendPasswordResetEmail(String email) {
//        Optional<User> user = this.userRepository.findByEmail(email);
//        if(user.isEmpty() || !user.get().getIsConfirmed()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("user.notFound", null, Locale.getDefault()));
//        }
//        String  mailMessage =
//                """
//               <html>
//                 <head>
//                   <link rel="preconnect" href="https://fonts.googleapis.com">
//                   <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
//                   <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300&display=swap" rel="stylesheet">
//                 </head>
//                 <body style="font-family: 'Roboto', sans-serif;height:100%; color:black; display:flex; justify-content: center; align-items: center; padding:1.5em 10em; background-color:#1a1a1a">
//                   <br>
//                   <br>
//                   <div style="width:100%;height:350px; background-color:white;text-align:center;border-radius:1.2em; ">
//                     <h1 style="font-size:xxx-large;">Password Reset</h1>
//                     <p style="font-size:x-large;">To reset your password, click on the button below:<p/>
//                     <br>
//                     <br>
//                     <form action='http://localhost:5173/passwordReset/""" + user.get().getId() + "'" + """
//                       >
//                       <input type="submit" style="border-radius:1.2em; cursor:pointer; padding:1em 2em; background-color:#00ADB5; color:white; font-size:x-large; font-family: Arial, Helvetica, sans-serif; border:none;" value="Password Reset"/>
//                     </form>
//                     <br>
//                   </div>
//                   <br>
//                   <br>
//                 </body>
//               </html>
//                """;
//        try {
//            boolean sentEmail = this.mailService.sendTextEmail(
//                    user.get().getEmail(),
//                    "Password Reset",
//                    mailMessage
//            );
//            if (!sentEmail) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("reset.notSent", null, Locale.getDefault()));
//            }
//        } catch(IOException ex) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,messageSource.getMessage("reset.notSent", null, Locale.getDefault()));
//        }
//
//    }
//
//    @Override
//    public void resetPassword(PasswordResetDTO newPassword) {
//        Optional<User> user = this.userRepository.findById(newPassword.getUserId());
//        if(user.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("user.notFound", null, Locale.getDefault()));
//        }
//        user.get().setPassword(passwordEncoder().encode(newPassword.getPassword()));
//        if(user.get().getRoles().get(0).getName().equals("ROLE_SUPERADMIN")) {
//            String path = env.getProperty("tempfolder.path");
//            File pwFile = new File(path + "/pwfile.txt");
//            pwFile.delete();
//            user.get().setIsConfirmed(true);
//        }
//        userRepository.save(user.get());
//    }

    @Override
    public void sendPasswordResetEmail(String email) {
        Optional<User> user = this.userRepository.findByEmail(email);
        if (user.isEmpty() || !user.get().getIsConfirmed()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("user.notFound", null, Locale.getDefault()));
        }

        // Tạo thông điệp email
        String mailMessage = String.format("""
        <html>
            <head>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300&display=swap" rel="stylesheet">
            </head>
            <body style="font-family: 'Roboto', sans-serif;height:100%%; color:black; display:flex; justify-content: center; align-items: center; padding:1.5em 10em; background-color:#1a1a1a">
                <div style="width:100%%;height:350px; background-color:white;text-align:center;border-radius:1.2em;">
                    <h1 style="font-size:xxx-large;">Password Reset</h1>
                    <p style="font-size:x-large;">To reset your password, click on the button below:</p>
                    <form action='http://localhost:5173/passwordReset/%d'>
                        <input type="submit" style="border-radius:1.2em; cursor:pointer; padding:1em 2em; background-color:#00ADB5; color:white; font-size:x-large; font-family: Arial, Helvetica, sans-serif; border:none;" value="Password Reset"/>
                    </form>
                </div>
            </body>
        </html>
        """, user.get().getId());  // Lưu ý cách format lại id người dùng vào URL

        // Gửi email
        boolean sentEmail = this.mailService.sendTextEmail(
                user.get().getEmail(),
                "Password Reset",
                mailMessage
        );

        if (!sentEmail) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("reset.notSent", null, Locale.getDefault()));
        }
    }

    @Override
    public void resetPassword(PasswordResetDTO newPassword) {
        Optional<User> user = this.userRepository.findById(newPassword.getUserId());
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("user.notFound", null, Locale.getDefault()));
        }

        // Băm mk mới sau đó lưu db
        user.get().setPassword(passwordEncoder().encode(newPassword.getPassword()));

        // Kiểm tra nếu là SuperAdmin, xóa file tạm và đánh dấu người dùng là đã xác nhận
        if (user.get().getRoles().get(0).getName().equals("ROLE_SUPERADMIN")) {
            String path = env.getProperty("tempfolder.path");
            File pwFile = new File(path + "/pwfile.txt");
            pwFile.delete();  // Xóa file tạm
            user.get().setIsConfirmed(true);  // Đánh dấu là đã xác nhận
        }

        // Lưu người dùng với mật khẩu mới
        userRepository.save(user.get());
    }


    @Override
    @Cacheable(value = "user", key = "#userId")
    public UserInfoDTO getUserInfo(Integer userId) {
        Optional<User> user = this.userRepository.findById(userId);
        if(user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("user.notFound", null, Locale.getDefault()));
        }
        return new UserInfoDTO(user.get().getId(),
                user.get().getUsername(),
                user.get().getName(),
                user.get().getSurname(),
                user.get().getEmail(),
                user.get().getProfilePicture(),
                user.get().getRoles().get(0).getName());
    }

    @Override
    public List<UserSearchInfo> findByKey(String key, Integer userId) {
        Optional<List<User>> users = userRepository.findTop10ByFullTextIsContaining(key.toLowerCase());
        List<UserSearchInfo> transformedUsers = new ArrayList<>();
        if(users.isPresent()) {
            for(User user : users.get()) {
                if(Objects.equals(user.getId(), userId)) continue;
                transformedUsers.add( new UserSearchInfo(user.getId(), user.getUsername(), user.getName() + " " + user.getSurname()));
            }
        }
        return transformedUsers;
    }

    @Override
    public Optional<User> getById(Integer id) {
        return userRepository.findById(id);
    }
}
