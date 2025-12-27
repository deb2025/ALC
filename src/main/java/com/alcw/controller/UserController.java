package com.alcw.controller;


import com.alcw.dto.ProfileUpdateResponse;
import com.alcw.dto.UpdateProfileDTO;
import com.alcw.dto.UserResponse;
import com.alcw.exception.InvalidCredentialsException;
import com.alcw.model.User;
import com.alcw.service.UserService;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final Cloudinary cloudinary;

    @GetMapping
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getOccupation().toString(),
                user.getMembershipId(),
                user.getProfileImageUrl()
        ));
    }


    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "occupation", required = false) String occupation,
            @RequestPart(value = "password", required = false) String password,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Principal principal) {

        UpdateProfileDTO updateDto = new UpdateProfileDTO();
        updateDto.setName(name);
        updateDto.setOccupation(occupation);
        updateDto.setPassword(password);

        // Validate image if present
        if (image != null) {
            validateImage(image);
        }

        User updatedUser = userService.updateProfile(principal.getName(), updateDto, image);

        return ResponseEntity.ok(new ProfileUpdateResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getOccupation().toString(),
                updatedUser.getProfileImageUrl(),
                "Update successful"
        ));
    }

    private void validateImage(MultipartFile image) {
        // Validate file type
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidCredentialsException("Only image files are allowed");
        }

        // Validate file size (max 5MB)
        if (image.getSize() > 5 * 1024 * 1024) {
            throw new InvalidCredentialsException("Image size must be less than 5MB");
        }
    }
}
