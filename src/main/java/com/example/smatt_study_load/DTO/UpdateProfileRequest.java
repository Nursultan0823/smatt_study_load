package com.example.smatt_study_load.DTO;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String whatsApp;
}
