package com.example.smatt_study_load.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.UpdateProfileRequest;
import com.example.smatt_study_load.DTO.UpdatedProfileDto;
import com.example.smatt_study_load.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PatchMapping("/me")
    public UpdatedProfileDto updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        return profileService.updateProfile(request, authentication);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAvatar(
            @RequestParam MultipartFile file,
            Authentication authentication
    ) {
        profileService.updateAvatar(file, authentication);
        return ResponseEntity.ok(new Response("Фото профиля обновлено"));
    }

    @GetMapping("/avatar")
    public ResponseEntity<byte[]> getAvatar(Authentication authentication) {
        return profileService.getAvatar(authentication);
    }
}
