package com.example.smatt_study_load.DTO;

import lombok.Data;

@Data
public class RegisterStudentRequest {
    private String fullName;
    private String email;
    private String password;
    private int groupId;
}
