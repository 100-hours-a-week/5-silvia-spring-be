package com.avoworld.controller;

import com.avoworld.entity.User;
import com.avoworld.service.FileStorageService;
import com.avoworld.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable("userId") Long userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
    }

    @PutMapping("/{userId}/nickname")
    public ResponseEntity<?> updateNickname(@PathVariable("userId") Long userId, @RequestBody Map<String, String> body) {
        String nickname = body.get("nickname");
        if (nickname == null || nickname.isEmpty()) {
            return ResponseEntity.badRequest().body("Nickname cannot be empty");
        }
        userService.updateNickname(userId, nickname);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable("userId") Long userId, @RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body("password cannot be empty");
        }
        userService.updatePassword(userId, password);
        return ResponseEntity.ok().build();
    }
//    public void updatePassword(@PathVariable("userId") Long userId, @RequestBody String password) {
//        userService.updatePassword(userId, password);
//    }



    @PostMapping("/{userId}/profileimg")
    public ResponseEntity<String> updateProfilePicture(@PathVariable("userId") Long userId, @RequestParam("profileimg") MultipartFile profileImg) throws IOException {
        // Save the file to a desired location and get the URL or path
        String profilePictureUrl = fileStorageService.saveProfileImage(profileImg);

        // Update the profile picture URL in the database
        userService.updateProfilePicture(userId, profilePictureUrl);

        return ResponseEntity.ok("Profile picture updated successfully");
    }

//    private String saveProfileImage(MultipartFile profileImg) throws IOException {
//        String uploadsDir = "/uploads/";
//        Path uploadsPath = Paths.get(uploadsDir);
//
//        // Ensure the directory exists
//        if (!Files.exists(uploadsPath)) {
//            Files.createDirectories(uploadsPath);
//        }
//
//        String filePath = uploadsPath.resolve(profileImg.getOriginalFilename()).toString();
//        File dest = new File(filePath);
//        profileImg.transferTo(dest);
//
//        // Return the URL or path of the saved file
//        return filePath;
//    }


    @PostMapping("/check-email")
    public Map<String, Boolean> checkEmailDuplicate(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean isDuplicate = userService.isEmailDuplicate(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return response;
    }

}
