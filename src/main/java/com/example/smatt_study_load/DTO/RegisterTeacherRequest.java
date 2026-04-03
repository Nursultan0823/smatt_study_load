package com.example.smatt_study_load.DTO;

import lombok.Data;

@Data
public class RegisterTeacherRequest {
    private String fullName;
    private String email;
    private String password;
    private String position;
 
}
