package com.example.smatt_study_load.DTO;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
     private String groupName;
     private String role;
}
