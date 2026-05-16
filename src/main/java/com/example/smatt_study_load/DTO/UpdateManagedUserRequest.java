package com.example.smatt_study_load.DTO;

import lombok.Data;

@Data
public class UpdateManagedUserRequest {
    private Integer groupId;
    private Boolean groupLeader;
    private String teacherPosition;
}
