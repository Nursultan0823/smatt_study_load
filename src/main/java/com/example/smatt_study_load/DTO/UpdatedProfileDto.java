package com.example.smatt_study_load.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatedProfileDto {
    private Integer id;
    private String fullName;
    private String email;
    private String token;
    private boolean hasAvatar;
    private String phoneNumber;
    private String whatsApp;
}
