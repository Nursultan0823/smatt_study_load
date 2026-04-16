package com.example.smatt_study_load.DTO;

import java.util.List;

import lombok.Data;

@Data
public class CurrentStudentDTO {

    private Integer id;
    private String fullName;
    private String email;
    private boolean enabled;
    private List<String> roles;
    private String group;
    private int studentId;
    private int groupId;

    public CurrentStudentDTO(Integer id, String fullName, String email, boolean enabled,
                             List<String> roles, String group, int studentId, int groupId) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
        this.group = group;
        this.studentId = studentId;
        this.groupId = groupId;
    }
}