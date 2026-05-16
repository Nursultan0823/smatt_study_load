package com.example.smatt_study_load.DTO;

import java.util.List;

import lombok.Data;

@Data
public class AdminManagedUserDto {
    private int id;
    private String fullName;
    private String email;
    private String status;
    private boolean enabled;
    private boolean confirmed;
    private List<String> roles;
    private String primaryRole;

    private Integer studentProfileId;
    private Integer groupId;
    private String groupName;
    private boolean groupLeader;

    private Integer teacherProfileId;
    private String teacherPosition;
    private String teacherPhoneNumber;
    private String teacherWhatsApp;
}
