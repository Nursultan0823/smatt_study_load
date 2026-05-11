package com.example.smatt_study_load.DTO;

import java.util.List;

import lombok.Data;

@Data
public class CurrentTeacherDTO {
      private Integer id;
    private String fullName;
    private String email;
    private boolean enabled;
    private List<String> roles;
    private String position;
    private String phoneNumber;
    private String whatsApp;
    private int teacherId;

     public CurrentTeacherDTO(Integer id, String fullName, String email, boolean enabled, List<String> roles,String position,String phoneNumber,String whatsApp,int teacherId) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
        this.position=position;
        this.phoneNumber=phoneNumber;
        this.whatsApp=whatsApp;
        this.teacherId=teacherId;
    }
}
