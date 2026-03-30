package com.example.smatt_study_load.DTO;

import java.util.List;

import lombok.Data;

@Data
public class CurrentUserDto {
      private Integer id;
    private String fullName;
    private String email;
    private boolean enabled;
    private List<String> roles;
     public CurrentUserDto(Integer id, String fullName, String email, boolean enabled, List<String> roles) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
    }
}
