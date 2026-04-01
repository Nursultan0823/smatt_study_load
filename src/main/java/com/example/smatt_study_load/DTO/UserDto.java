package com.example.smatt_study_load.DTO;

import java.util.Set;

import com.example.smatt_study_load.models.Roles;

import lombok.Data;

@Data
public class UserDto {
     private Integer id;
     private String fullName;
     private String email;
    private String status;
    private Set<Roles> roles;
      public UserDto(Integer id, String fullName, String email, String status,Set<Roles> roles) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
        this.roles=roles;
    }

}
